package com.kjh.mynote.ui.features.map

import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityNaverMapBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.utils.extensions.hideKeyboard
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

@AndroidEntryPoint
class NaverMapActivity
    : BaseActivity<ActivityNaverMapBinding>({ ActivityNaverMapBinding.inflate(it) }), OnMapReadyCallback {

    private val viewModel: NaverMapViewModel by viewModels()

    private var marker: Marker? = null
    private lateinit var naverMap: NaverMap
    private lateinit var sheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val searchResultListAdapter: NaverMapSearchResultListAdapter by lazy {
        NaverMapSearchResultListAdapter(onPlaceClickAction)
    }

    override fun onInitView() {
        onInitNaverMap()

        with(binding) {
            sheetBehavior = BottomSheetBehavior.from(binding.include.bottomSheet).apply {
                peekHeight = resources.getDimensionPixelSize(R.dimen.peek_height)
                state = BottomSheetBehavior.STATE_HIDDEN
                addBottomSheetCallback(bottomSheetCallBack)
            }

            include.rvPlaceSearchResults.apply {
                adapter = searchResultListAdapter
            }

            etQuery.setOnEditorActionListener(searchEditorActionListener)
            ibSearch.setOnThrottleClickListener(searchButtonClickListener)
            btnShowList.setOnThrottleClickListener(showResultListButtonClickListener)
        }
    }

    private fun onInitNaverMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.fcv_map_container) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.fcv_map_container, it).commit()
            }

        mapFragment.getMapAsync(this@NaverMapActivity)
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.naverMapSearchState
                        .collect { searchState ->
                            with(binding.include) {
                                flLoading.isVisible = searchState is NaverMapSearchState.Searching
                                tvEmpty.isVisible = searchState is NaverMapSearchState.Nothing
                            }

                            setSheetBehaviorCollapsed()
                        }
                }

                launch {
                    viewModel.placeItems
                        .collect { placeItems ->
                            searchResultListAdapter.submitList(placeItems) {
                                setSheetBehaviorCollapsed()
                            }
                        }
                }

                launch {
                    viewModel.selectPlaceItemEvent.collect(::updateCameraAndMakeMarker)
                }
            }
        }
    }

    override fun onDestroy() {
        naverMap.removeOnCameraChangeListener(cameraChangeListener)
        sheetBehavior.removeBottomSheetCallback(bottomSheetCallBack)
        super.onDestroy()
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        this.naverMap.addOnCameraChangeListener(cameraChangeListener)
    }

    private fun updateCameraAndMakeMarker(placeItem: KakaoPlaceModel) {
        marker?.let { it.map = null }

        val targetLatLng = LatLng(placeItem.y.toDouble(), placeItem.x.toDouble())
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(targetLatLng, 14.0)
            .animate(CameraAnimation.None)
            .finishCallback {
                marker = Marker().apply {
                    position = targetLatLng
                    width = 60
                    height = 80
                    icon = MarkerIcons.BLACK
                    iconTintColor = ContextCompat.getColor(this@NaverMapActivity, R.color.purple)
                    captionText = placeItem.placeName
                    captionColor = ContextCompat.getColor(this@NaverMapActivity, R.color.purple)
                    map = naverMap
                }

                setSheetBehaviorCollapsed()
            }

        naverMap.moveCamera(cameraUpdate)
    }

    private fun checkValidQuery(query: String) {
        if (query.isBlank()) {
            showToast("장소 혹은 주소를 입력해주세요.")
        } else {
            binding.etQuery.clearFocus()
            binding.etQuery.hideKeyboard()

            viewModel.getPlacesByQuery(query)
        }
    }

    private fun setSheetBehaviorCollapsed() {
        if (sheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private val bottomSheetCallBack = object: BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
               BottomSheetBehavior.STATE_COLLAPSED -> {
                   binding.btnShowList.isVisible = false
               }
               BottomSheetBehavior.STATE_HIDDEN -> {
                   binding.btnShowList.isVisible = true
               }
            }
        }
    }

    private val onPlaceClickAction: (KakaoPlaceModel) -> Unit = { item ->
        viewModel.setSelectPlace(item)
    }

    private val cameraChangeListener = NaverMap.OnCameraChangeListener { reason, _ ->
        if (reason == -1 && sheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            binding.btnShowList.isVisible = true
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private val searchEditorActionListener = OnEditorActionListener { view, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            checkValidQuery(query = view.text.toString())
            true
        } else {
            false
        }
    }

    private val searchButtonClickListener = OnClickListener {
        checkValidQuery(query = binding.etQuery.text.toString())
    }

    private val showResultListButtonClickListener = OnClickListener {
        setSheetBehaviorCollapsed()
        binding.btnShowList.isVisible = false
    }
}
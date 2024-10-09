package com.kjh.mynote.ui.features.map

import android.content.Intent
import android.view.View
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.activity.viewModels
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
import com.kjh.mynote.ui.base.BaseNaverMapActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.constants.AppConstants.DEFAULT_ZOOM_LEVEL
import com.kjh.mynote.utils.extensions.hideKeyboard
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

@AndroidEntryPoint
class NaverMapActivity
    : BaseNaverMapActivity<ActivityNaverMapBinding>({ ActivityNaverMapBinding.inflate(it) }) {

    private val viewModel: NaverMapViewModel by viewModels()

    private var marker: Marker? = null
    private lateinit var sheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val searchResultListAdapter: NaverMapSearchResultListAdapter by lazy {
        NaverMapSearchResultListAdapter(onPlaceClickAction, onSelectClickAction)
    }

    override fun onInitView() {
        with(binding) {
            sheetBehavior = BottomSheetBehavior.from(binding.include.bottomSheet).apply {
                peekHeight = resources.getDimensionPixelSize(R.dimen.peek_height)
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

    override fun onInitUiData() {
        viewModel.checkInitData()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.isLoading }
                        .distinctUntilChanged()
                        .collect { isLoading ->
                            binding.cpiLoading.isVisible = isLoading
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isEmpty }
                        .distinctUntilChanged()
                        .collect { isResultEmpty ->
                            if (isResultEmpty) {
                                showToast("검색 결과가 없습니다.")
                                viewModel.shownEmptyToast()
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.sheetBehavior }
                        .distinctUntilChanged()
                        .collect { sheetState ->
                            sheetBehavior.state = sheetState
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.placeItems }
                        .distinctUntilChanged()
                        .collect { placeItems ->
                            searchResultListAdapter.submitList(placeItems)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.movingCameraPlaceItem }
                        .distinctUntilChanged()
                        .filterNotNull()
                        .collect {
                            updateCamera(it)
                        }
                }

                launch {
                    viewModel.showOpenButtonFlow
                        .collect { isShow ->
                            binding.btnShowList.isVisible = isShow
                        }
                }
            }
        }
    }

    override fun onDestroy() {
        sheetBehavior.removeBottomSheetCallback(bottomSheetCallBack)
        super.onDestroy()
    }

    private fun updateCamera(placeItem: KakaoPlaceModel) {
        clearPrevMarker()

        val targetLatLng = LatLng(placeItem.y.toDouble(), placeItem.x.toDouble())
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(targetLatLng, DEFAULT_ZOOM_LEVEL)
            .animate(CameraAnimation.None)

        marker = getMarker(placeItem, targetLatLng)
        naverMap.moveCamera(cameraUpdate)

        binding.root.post {
            viewModel.clearMovingCameraPlaceItemWithStateCollapsed()
        }
    }

    private fun getMarker(
        placeItem: KakaoPlaceModel,
        latLng: LatLng
    ): Marker = Marker().apply {
        position = latLng
        width = 60
        height = 80
        icon = MarkerIcons.BLACK
        iconTintColor = ContextCompat.getColor(this@NaverMapActivity, R.color.purple)
        captionText = placeItem.placeName
        captionColor = ContextCompat.getColor(this@NaverMapActivity, R.color.purple)
        map = naverMap
    }

    private fun clearPrevMarker() {
        marker?.let { it.map = null }
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

    override fun onCameraChange(reason: Int, p1: Boolean) {
        if (reason == -1) {
            viewModel.setSheetBehaviorTo(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    private val bottomSheetCallBack = object: BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED ->
                    viewModel.setSheetBehaviorTo(BottomSheetBehavior.STATE_COLLAPSED)
                BottomSheetBehavior.STATE_EXPANDED ->
                    viewModel.setSheetBehaviorTo(BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_HIDDEN ->
                    viewModel.setSheetBehaviorTo(BottomSheetBehavior.STATE_HIDDEN)
            }
        }
    }

    private val onPlaceClickAction: (KakaoPlaceModel) -> Unit = { item ->
        viewModel.setMovingCameraPlaceItem(item)
    }

    private val onSelectClickAction: (KakaoPlaceModel) -> Unit = { item ->
        Intent().apply {
            putExtra(AppConstants.INTENT_TEMP_PLACE_ITEM, item)
            setResult(RESULT_OK, this)
            finish()
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
        viewModel.setSheetBehaviorTo(BottomSheetBehavior.STATE_COLLAPSED)
    }
}
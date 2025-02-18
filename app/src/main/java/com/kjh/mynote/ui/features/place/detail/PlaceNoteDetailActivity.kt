package com.kjh.mynote.ui.features.place.detail

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceNoteDetailBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.DefaultDialog
import com.kjh.mynote.ui.features.place.detail.adapter.PlaceNoteDetailUiListAdapter
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.place.map.PlaceMapActivity
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setDarkStatusBar
import com.kjh.mynote.utils.extensions.setLightStatusBar
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlaceNoteDetailActivity :
    BaseActivity<ActivityPlaceNoteDetailBinding>({ ActivityPlaceNoteDetailBinding.inflate(it) }) {

    private val viewModel: PlaceNoteDetailViewModel by viewModels()

    private val uiListAdapter: PlaceNoteDetailUiListAdapter by lazy {
        PlaceNoteDetailUiListAdapter(
            imageViewerClickAction = imageViewerClickAction,
            addressClickAction = addressClickAction,
            samePlaceItemClickAction = samePlaceItemClickAction,
            purchaseNoteItemClickAction = purchaseNoteItemClickAction
        )
    }

    private var isAppliedInset = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun onInitView() {
        with (binding) {
            ViewCompat.setOnApplyWindowInsetsListener(tbToolbar) { v, insets ->
                if (!isAppliedInset) {
                    val systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                    v.setPadding(v.left, systemBarsTop, v.right, v.bottom)
                }

                isAppliedInset = true
                insets
            }

            rvPlaceDetails.apply {
                adapter = uiListAdapter
                addOnScrollListener(onScrollListener)
            }

            tbToolbar.setMoreButtonClickListener(toolbarMoreButtonClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest(::showToast)
                }

                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        when (uiState) {
                            PlaceNoteDetailUiState.Loading ->
                                binding.layoutLoading.root.makeVisible()

                            PlaceNoteDetailUiState.NotExist -> {
                                binding.layoutLoading.root.makeGone()
                                finish()
                            }
                            PlaceNoteDetailUiState.Error ->
                                binding.layoutLoading.root.makeGone()

                            is PlaceNoteDetailUiState.Success -> {
                                binding.layoutLoading.root.makeGone()
                                uiListAdapter.submitList(uiState.uiItems)
                            }
                        }
                    }
                }

                launch {
                    viewModel.deleteEventState.collectLatest { eventState ->
                        when (eventState) {
                            DeletePlaceNoteEventState.Loading ->
                                binding.layoutLoading.root.makeVisible()

                            is DeletePlaceNoteEventState.Error ->
                                binding.layoutLoading.root.makeGone()

                            DeletePlaceNoteEventState.Success -> {
                                binding.layoutLoading.root.makeGone()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        binding.rvPlaceDetails.removeOnScrollListener(onScrollListener)
        super.onDestroy()
    }

    private fun goToMakeOrModifyPlaceNotePage() {
        Intent(this@PlaceNoteDetailActivity, MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_NOTE_ID, viewModel.noteId)
            startActivity(this)
        }
    }

    private fun showProcessDeleteDialog() {
        DefaultDialog.newInstance(
            title = getString(R.string.will_you_delete),
            posBtnText = getString(R.string.yes_i_will_delete),
            negBtnText = getString(R.string.cancel),
            positiveClickAction = { viewModel.deletePlaceNote() }
        ).show(supportFragmentManager, DefaultDialog.TAG)
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val totalScroll = recyclerView.computeVerticalScrollOffset()
            val pagerHeight = resources.getDimensionPixelSize(R.dimen.place_detail_pager_height)
            val toolbarHeight = binding.tbToolbar.height
            val progress = (totalScroll / (pagerHeight - toolbarHeight).toFloat()).coerceIn(0f, 1f)

            val firstVisiblePosition =
                (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                    ?: -1
            if (firstVisiblePosition > NO_POSITION) {
                val isDetailUiFirst =
                    uiListAdapter.currentList[firstVisiblePosition] is PlaceNoteDetailUiItemState.PlaceDetailInfoItem

                if (progress >= 1f || !isDetailUiFirst) {
                    setDarkStatusBar()
                    binding.tbToolbar.background.alpha = 255
                } else {
                    setLightStatusBar()
                    binding.tbToolbar.background.alpha = 0
                }
            }
        }
    }

    private val imageViewerClickAction: (List<String>, String) -> Unit = { images, clickedImage ->
        Intent(this@PlaceNoteDetailActivity, ImagesViewerActivity::class.java).apply {
            putExtra(AppConstants.INTENT_IMAGE_LIST, ArrayList(images))
            putExtra(AppConstants.INTENT_URL, clickedImage)
            startActivity(this)
        }
    }

    private val addressClickAction: (PlaceNoteUiModel) -> Unit = { item ->
        Intent(this@PlaceNoteDetailActivity, PlaceMapActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_INFO_ITEM, item.placeInfo)
            startActivity(this)
        }
    }

    private val samePlaceItemClickAction: (PlaceNoteUiModel) -> Unit = { item ->
        Intent(this@PlaceNoteDetailActivity, PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, item.id)
            startActivity(this)
        }
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { item ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, item.id)
            startActivity(this)
        }
    }

    private val toolbarMoreButtonClickListener = OnClickListener {
        PlaceNoteDetailMenuBSDialog.newInstance(
            deleteClickAction = { showProcessDeleteDialog() },
            modifyClickAction = { goToMakeOrModifyPlaceNotePage() }
        ).show(supportFragmentManager, PlaceNoteDetailMenuBSDialog.TAG)
    }
}
package com.kjh.mynote.ui.features.place.detail

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceNoteDetailBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.DefaultDialog
import com.kjh.mynote.ui.common.dialog.DefaultDialog.MyDefaultDialogEventListener
import com.kjh.mynote.ui.features.place.detail.adapter.PlaceNoteDetailUiListAdapter
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.ui.features.place.map.PlaceMapActivity
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.setDarkStatusBar
import com.kjh.mynote.utils.extensions.setLightStatusBar
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlaceNoteDetailActivity :
    BaseActivity<ActivityPlaceNoteDetailBinding>({ ActivityPlaceNoteDetailBinding.inflate(it) }),
    PlaceNoteDetailMenuBSDialog.PlaceNoteDetailMenuClickListener,
    MyDefaultDialogEventListener
{
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
                setHasFixedSize(true)
                adapter = uiListAdapter
                addOnScrollListener(onScrollListener)
            }

            tbToolbar.setMoreButtonClickListener(toolbarMoreButtonClickListener)
        }
    }

    override fun onInitUiData() {
        viewModel.getPlaceNoteDetail()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is PlaceNoteDetailUiState.Loading -> {}
                            is PlaceNoteDetailUiState.NotExist -> {
                                with (binding) {
                                    motionLayout.progress = 1f
                                    tbToolbar.isShowMoreButton = false
                                    tvDeletedNotes.isVisible = true
                                }
                            }
                            is PlaceNoteDetailUiState.Error -> {
                                showToast(uiState.msg)
                                viewModel.shownGetPlaceNoteDetailError()
                            }
                            is PlaceNoteDetailUiState.Success -> {
                                uiListAdapter.submitList(uiState.placeNoteDetailUiItems)
                            }
                        }
                    }
                }

                launch {
                    viewModel.requestDeleteEventState.collectLatest{ state ->
                        when (state) {
                            is UiState.Error -> {
                                showToast(state.errorMsg)
                            }
                            is UiState.Success -> {
                                Intent().apply {
                                    putExtra(AppConstants.INTENT_NOTE_ID, state.data)
                                    setResult(RESULT_OK, this)
                                    finish()
                                }
                            }
                            else -> {}
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

    private fun showDeleteOrModifyDialog() {
        PlaceNoteDetailMenuBSDialog.newInstance()
            .show(supportFragmentManager, PlaceNoteDetailMenuBSDialog.TAG)
    }

    private val modifyResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = { result ->
            val updatedNoteItem =
                result.data?.parcelable<PlaceNoteUiModel>(AppConstants.INTENT_PLACE_NOTE_ITEM)
            updatedNoteItem?.let {
                viewModel.getPlaceNoteDetail()
            }
        }
    )

    private val purchaseNoteDetailResultLauncher = registerStartActivityResultLauncher(
        resultOkBlock = {
            viewModel.getPlaceNoteDetail()
        }
    )

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val totalScroll = recyclerView.computeVerticalScrollOffset()
            val pagerHeight = resources.getDimensionPixelSize(R.dimen.place_detail_pager_height)
            val toolbarHeight = binding.tbToolbar.height
            val progress = (totalScroll / (pagerHeight - toolbarHeight).toFloat()).coerceIn(0f, 1f)

            binding.motionLayout.progress = progress

            if (progress >= 1f) {
                setDarkStatusBar()
            } else {
                setLightStatusBar()
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
            purchaseNoteDetailResultLauncher.launch(this)
        }
    }

    private val toolbarMoreButtonClickListener = OnClickListener {
        showDeleteOrModifyDialog()
    }

    override fun onClickDeleteMenu() {
        DefaultDialog.newInstance(
            title = getString(R.string.will_you_delete),
            posBtnText = getString(R.string.yes_i_will_delete),
            negBtnText = getString(R.string.cancel)
        ).show(supportFragmentManager, DefaultDialog.TAG)
    }

    override fun onClickModifyMenu() {
        val uiState = viewModel.uiState.value
        if (uiState is PlaceNoteDetailUiState.Success) {
            uiState.placeNoteItem?.let {
                Intent(this@PlaceNoteDetailActivity, MakeOrModifyPlaceNoteActivity::class.java).apply {
                    putExtra(AppConstants.INTENT_PLACE_NOTE_ITEM, it)
                    modifyResultLauncher.launch(this)
                }
            }
        }
    }

    override fun onDialogPositiveClick() {
        viewModel.deletePlaceNote()
    }

    override fun onDialogNegativeClick() {}
    override fun onDialogDismiss() {}
}
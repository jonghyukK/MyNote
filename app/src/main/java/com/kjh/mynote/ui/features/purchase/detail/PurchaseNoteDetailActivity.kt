package com.kjh.mynote.ui.features.purchase.detail

import android.content.Intent
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPurchaseNoteDetailBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.DefaultDialog
import com.kjh.mynote.ui.common.dialog.DefaultDialog.MyDefaultDialogEventListener
import com.kjh.mynote.ui.features.place.map.PlaceMapActivity
import com.kjh.mynote.ui.features.purchase.detail.adapter.PurchaseNoteDetailImageListAdapter
import com.kjh.mynote.ui.features.purchase.edit.EditPurchaseNoteActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.registerStartActivityResultLauncher
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteDetailActivity: BaseActivity<ActivityPurchaseNoteDetailBinding>(
    { ActivityPurchaseNoteDetailBinding.inflate(it) }),
    MyDefaultDialogEventListener
{

    private val viewModel: PurchaseNoteDetailViewModel by viewModels()

    private val imageListAdapter: PurchaseNoteDetailImageListAdapter by lazy {
        PurchaseNoteDetailImageListAdapter(imageClickAction = imageClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvImages.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(right = 12, exceptFirstItem = false))
                adapter = imageListAdapter
            }

            tbToolbar.setRightFirstButtonClickListener(deleteNoteClickListener)
            tbToolbar.setRightSecondButtonClickListener(editNoteClickListener)
            tvPurchasePlace.setTextClickListener(placeClickListener)
        }
    }

    override fun onInitUiData() {
        viewModel.getPurchaseNoteById()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.purchaseNoteItem }
                        .filterNotNull()
                        .collect { purchaseNoteItem ->
                            setupPurchaseNoteUi(purchaseNoteItem)
                        }
                }

                launch {
                    viewModel.deleteEventState.collectLatest { deleteEventState ->
                        when (deleteEventState) {
                            is UiState.Error -> {
                                showToast(deleteEventState.errorMsg)
                            }
                            is UiState.Success -> {
                                Intent().apply {
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

    private fun setupPurchaseNoteUi(purchaseNoteItem: PurchaseNoteUiModel) = with (binding) {
        tvPurchaseName.text = purchaseNoteItem.purchaseName
        tvCategory.text = purchaseNoteItem.category?.categoryName ?: "카테고리 없음"
        tvPurchaseDate.text = purchaseNoteItem.purchaseDate.toStringWithFormat("yyyy년 M월 d일 (E)")
        tvPurchasePrice.text = getString(R.string.format_won, purchaseNoteItem.purchasePrice.toComma())

        purchaseNoteItem.placeInfo?.let { placeInfo ->
            tvPurchasePlace.isVisible = true
            tvPurchasePlace.text = placeInfo.placeName
        } ?: run {
            tvPurchasePlace.isVisible = false
        }

        if (purchaseNoteItem.images.isNullOrEmpty()) {
            rvImages.visibility = View.GONE
        } else {
            rvImages.visibility = View.VISIBLE
            imageListAdapter.submitList(purchaseNoteItem.images)
        }
    }

    private val editNoteResultLauncher = registerStartActivityResultLauncher(resultOkBlock = {
        viewModel.getPurchaseNoteById()

        Intent().apply {
            setResult(RESULT_OK, this)
        }
    })

    private val imageClickAction: (String) -> Unit = { clickedImage ->
        val images = viewModel.uiState.value.purchaseNoteItem?.images ?: emptyList()

        Intent(this, ImagesViewerActivity::class.java).apply {
            putExtra(AppConstants.INTENT_IMAGE_LIST, ArrayList(images))
            putExtra(AppConstants.INTENT_URL, clickedImage)
            startActivity(this)
        }
    }

    private val deleteNoteClickListener = OnClickListener {
        DefaultDialog.newInstance(
            title = getString(R.string.will_you_delete_this_purchase_note),
            posBtnText = getString(R.string.yes_i_will_delete),
            negBtnText = getString(R.string.cancel)
        ).show(supportFragmentManager, DefaultDialog.TAG)
    }

    private val editNoteClickListener = OnClickListener {
        Intent(this, EditPurchaseNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ITEM, viewModel.uiState.value.purchaseNoteItem)
            editNoteResultLauncher.launch(this)
        }
    }

    private val placeClickListener = OnClickListener {
        Intent(this, PlaceMapActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_INFO_ITEM, viewModel.uiState.value.purchaseNoteItem?.placeInfo)
            startActivity(this)
        }
    }

    override fun onDialogPositiveClick() {
        viewModel.deletePurchaseNote()
    }

    override fun onDialogNegativeClick() {}
    override fun onDialogDismiss() {}
}
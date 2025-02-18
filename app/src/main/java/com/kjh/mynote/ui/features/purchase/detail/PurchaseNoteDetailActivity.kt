package com.kjh.mynote.ui.features.purchase.detail

import android.content.Intent
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPurchaseNoteDetailBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.DefaultDialog
import com.kjh.mynote.ui.features.place.map.PlaceMapActivity
import com.kjh.mynote.ui.features.purchase.detail.adapter.PurchaseNoteDetailImageListAdapter
import com.kjh.mynote.ui.features.purchase.makeedit.MakeEditPurchaseNoteActivity
import com.kjh.mynote.ui.features.viewer.ImagesViewerActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteDetailActivity : BaseActivity<ActivityPurchaseNoteDetailBinding>(
    { ActivityPurchaseNoteDetailBinding.inflate(it) }) {

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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is PurchaseNoteDetailUiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is PurchaseNoteDetailUiState.NotExist -> {
                                binding.layoutLoading.root.makeGone()
                                finish()
                            }
                            is PurchaseNoteDetailUiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                            }
                            is PurchaseNoteDetailUiState.Success -> {
                                binding.layoutLoading.root.makeGone()
                                setupPurchaseNoteUi(uiState.purchaseNoteItem)
                            }
                        }
                    }
                }

                launch {
                    viewModel.deleteEventState.collectLatest {
                        when (it) {
                            DeletePurchaseNoteEventState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is DeletePurchaseNoteEventState.Error -> {
                                binding.layoutLoading.root.makeGone()
                            }
                            DeletePurchaseNoteEventState.Success -> {
                                binding.layoutLoading.root.makeGone()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupPurchaseNoteUi(purchaseNoteItem: PurchaseNoteUiModel) = with (binding) {
        // 구매명
        tvPurchaseName.text = purchaseNoteItem.purchaseName
        // 카테고리
        tvCategory.text = purchaseNoteItem.category?.categoryName ?: getString(R.string.nothing_category)
        // 결제수단
        tvPaymentMethod.text = purchaseNoteItem.paymentMethod?.paymentMethodName ?: getString(R.string.nothing_payment_method)
        // 구매 날짜
        tvPurchaseDate.text = purchaseNoteItem.purchaseDate.toStringWithFormat(AppConstants.DATE_FORMAT_YYYY_M_D_E)
        // 구매 가격
        tvPurchasePrice.text = getString(R.string.format_won, purchaseNoteItem.purchasePrice.toComma())
        // 구매 장소
        tvPurchasePlace.apply {
            isVisible = purchaseNoteItem.placeInfo != null
            text = purchaseNoteItem.placeInfo?.placeName ?: ""
        }
        // 이미지
        rvImages.isVisible = !purchaseNoteItem.images.isNullOrEmpty()
        if (!purchaseNoteItem.images.isNullOrEmpty()) {
            imageListAdapter.submitList(purchaseNoteItem.images)
        }
    }

    private val imageClickAction: (String) -> Unit = { clickedImage ->
        val uiState = viewModel.uiState.value as? PurchaseNoteDetailUiState.Success
        uiState?.let {
            val images = it.purchaseNoteItem.images ?: return@let
            Intent(this, ImagesViewerActivity::class.java).apply {
                putExtra(AppConstants.INTENT_IMAGE_LIST, ArrayList(images))
                putExtra(AppConstants.INTENT_URL, clickedImage)
                startActivity(this)
            }
        }
    }

    private val deleteNoteClickListener = OnClickListener {
        DefaultDialog.newInstance(
            title = getString(R.string.will_you_delete_this_purchase_note),
            posBtnText = getString(R.string.yes_i_will_delete),
            negBtnText = getString(R.string.cancel),
            positiveClickAction = { viewModel.deletePurchaseNote() }
        ).show(supportFragmentManager, DefaultDialog.TAG)
    }

    private val editNoteClickListener = OnClickListener {
        val noteId = viewModel.purchaseNoteId
        if (noteId != -1) {
            Intent(this, MakeEditPurchaseNoteActivity::class.java).apply {
                putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, noteId)
                startActivity(this)
            }
        }
    }

    private val placeClickListener = OnClickListener {
        val uiState = viewModel.uiState.value as? PurchaseNoteDetailUiState.Success
        uiState?.let {
            val placeItem = it.purchaseNoteItem.placeInfo ?: return@OnClickListener
            Intent(this, PlaceMapActivity::class.java).apply {
                putExtra(AppConstants.INTENT_PLACE_INFO_ITEM, placeItem)
                startActivity(this)
            }
        }
    }
}
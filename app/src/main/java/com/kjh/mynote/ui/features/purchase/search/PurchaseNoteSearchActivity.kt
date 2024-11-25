package com.kjh.mynote.ui.features.purchase.search

import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityPurchaseNoteSearchBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchUiListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.date.PurchaseNoteDatePeriodFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.price.PurchaseNotePriceFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.purchasename.PurchaseNotePurchaseNameBSDialog
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteSearchActivity: BaseActivity<ActivityPurchaseNoteSearchBinding>({ ActivityPurchaseNoteSearchBinding.inflate(it) }) {

    private val viewModel: PurchaseNoteSearchViewModel by viewModels()

    private val resultListAdapter: PurchaseNoteSearchUiListAdapter by lazy {
        PurchaseNoteSearchUiListAdapter(
            purchaseNoteItemClickAction = purchaseNoteItemClickAction,
            categoryClickAction = categoryFilterClickAction,
            purchaseNameClickAction = purchaseNameClickAction,
            dateFilterClickAction = dateRangeFilterClickAction,
            priceFilterClickAction = priceFilterClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvList.apply {
                itemAnimator = null
//                addItemDecoration(SpacingItemDecoration(bottom = 16, exceptFirstItem = false))
                adapter = resultListAdapter
            }
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiList ->
                        resultListAdapter.submitList(uiList)
                    }
                }
            }
        }
    }

    private val categoryFilterClickAction: (Int) -> Unit = { categoryId ->
        viewModel.addOrDeleteCategoryItemBy(categoryId)
    }

    private val purchaseNameClickAction: () -> Unit = {
        PurchaseNotePurchaseNameBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNotePurchaseNameBSDialog.TAG)
    }

    private val dateRangeFilterClickAction: () -> Unit = {
        PurchaseNoteDatePeriodFilterBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNoteDatePeriodFilterBSDialog.TAG)
    }

    private val priceFilterClickAction: () -> Unit = {
        PurchaseNotePriceFilterBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNotePriceFilterBSDialog.TAG)
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { purchaseNoteItem ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, purchaseNoteItem.id)
            startActivity(this)
        }
    }

}
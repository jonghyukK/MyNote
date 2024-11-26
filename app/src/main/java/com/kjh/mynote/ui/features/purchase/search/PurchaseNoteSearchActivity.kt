package com.kjh.mynote.ui.features.purchase.search

import android.content.Intent
import android.view.View
import android.widget.ListAdapter
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPurchaseNoteSearchBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchSelectedFilterListAdapter
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchUiListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.date.PurchaseNoteDatePeriodFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.price.PurchaseNotePriceFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.purchasename.PurchaseNotePurchaseNameBSDialog
import com.kjh.mynote.utils.PurchaseNoteSearchItemDecoration
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

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
            priceFilterClickAction = priceFilterClickAction,
            selectedFilterClickAction = selectedFilterClickAction)
    }

    private val selectedFilterListAdapter: PurchaseNoteSearchSelectedFilterListAdapter by lazy {
        PurchaseNoteSearchSelectedFilterListAdapter(selectedFilterClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvList.apply {
                itemAnimator = null
                addItemDecoration(PurchaseNoteSearchItemDecoration())
                adapter = resultListAdapter
                addOnScrollListener(scrollListener)
            }

            layoutSelectedFilters.rvSelectedFilters.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(right = 8, exceptFirstItem = false))
                adapter = selectedFilterListAdapter
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

                launch {
                    viewModel.appliedFilterItem.collect {
                        selectedFilterListAdapter.submitList(it)
                    }
                }
            }
        }
    }

    private val scrollListener = object: OnScrollListener() {
        var stickyViewPosition = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val firstVisiblePosition = lm.findFirstVisibleItemPosition()

            if (firstVisiblePosition != RecyclerView.NO_POSITION) {
                if (isAppliedFiltersView(firstVisiblePosition)) {
                    stickyViewPosition = firstVisiblePosition
                    toggleFlattenSelectedFilterVisibility(true)
                } else {
                    if (firstVisiblePosition < stickyViewPosition) {
                        toggleFlattenSelectedFilterVisibility(false)
                    }
                }
            }
        }

        private fun isAppliedFiltersView(position: Int): Boolean =
            resultListAdapter.getItemViewType(position) ==
                    R.layout.vh_purchase_note_search_selected_filter_outer

        private fun toggleFlattenSelectedFilterVisibility(isVisible: Boolean) {
            binding.layoutSelectedFilters.root.isVisible = isVisible
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

    private val selectedFilterClickAction: (Filters) -> Unit = { filter ->
        viewModel.deleteFilter(filter)
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { purchaseNoteItem ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, purchaseNoteItem.id)
            startActivity(this)
        }
    }

}
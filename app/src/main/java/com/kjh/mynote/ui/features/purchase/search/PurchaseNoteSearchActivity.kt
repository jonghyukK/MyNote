package com.kjh.mynote.ui.features.purchase.search

import android.content.Intent
import android.graphics.Typeface
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.domain.model.SortType
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPurchaseNoteSearchBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.sort.SortBSDialog
import com.kjh.mynote.ui.common.dialog.sort.SortItem
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchResultListAdapter
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchSelectedFilterListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.category.PurchaseNoteSearchCategoryListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.date.PurchaseNoteDatePeriodFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.paymentmethod.PurchaseNoteSearchPaymentMethodListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.price.PurchaseNotePriceFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.purchasename.PurchaseNotePurchaseNameBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.whole.PurchaseNoteSearchWholeFilterBSDialog
import com.kjh.mynote.utils.decorations.PurchaseNotesUiStateItemDecoration
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteSearchActivity :
    BaseActivity<ActivityPurchaseNoteSearchBinding>({ ActivityPurchaseNoteSearchBinding.inflate(it) }),
    SortBSDialog.SortBSDialogClickListener {

    private val viewModel: PurchaseNoteSearchViewModel by viewModels()

    private val categoryListAdapter: PurchaseNoteSearchCategoryListAdapter by lazy {
        PurchaseNoteSearchCategoryListAdapter(categoryFilterClickAction)
    }

    private val paymentMethodListAdapter: PurchaseNoteSearchPaymentMethodListAdapter by lazy {
        PurchaseNoteSearchPaymentMethodListAdapter(paymentMethodFilterClickAction)
    }

    private val selectedFilterListAdapter: PurchaseNoteSearchSelectedFilterListAdapter by lazy {
        PurchaseNoteSearchSelectedFilterListAdapter(selectedFilterClickAction)
    }

    private val resultListAdapter: PurchaseNoteSearchResultListAdapter by lazy {
        PurchaseNoteSearchResultListAdapter(purchaseNoteItemClickAction)
    }

    override fun onInitView() {
        with (binding.layoutFilterContainer) {
            rvCategories.apply {
                itemAnimator = null
                adapter = categoryListAdapter
            }

            rvPaymentMethods.apply {
                itemAnimator = null
                adapter = paymentMethodListAdapter
            }

            tvPurchaseName.setOnThrottleClickListener(purchaseNameFilterClickListener)
            tvPrice.setOnThrottleClickListener(priceFilterClickListener)
        }

        with (binding.layoutFilterInfoSection) {
            clSortFilter.setOnThrottleClickListener(sortFilterClickListener)
            clConditionFilter.setOnThrottleClickListener(conditionFilterClickListener)
        }

        with (binding) {
            rvSelectedFilters.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(right = 8, exceptFirstItem = false))
                adapter = selectedFilterListAdapter
            }

            rvSearchResults.apply {
                itemAnimator = null
                addItemDecoration(PurchaseNotesUiStateItemDecoration())
                adapter = resultListAdapter
            }

            ivBack.setOnThrottleClickListener(backButtonClickListener)
            ivReset.setOnThrottleClickListener(selectedFilterResetBtnClickListener)
            llDateContainer.setOnThrottleClickListener(dateClickListener)
        }
    }

    override fun onInitUiData() {
        viewModel.getFilterUiState()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filterUiState
                        .filterIsInstance<FilterUiState.Success>()
                        .map { it.filters.dateRangeFilter }
                        .distinctUntilChanged()
                        .collect { dateFilter ->
                            binding.tvDate.text = dateFilter.dateRangeFilter.getUiText()
                        }
                }

                launch {
                    viewModel.filterUiState
                        .filterIsInstance<FilterUiState.Success>()
                        .map { it.filters.sortType }
                        .distinctUntilChanged()
                        .collect { sortType ->
                            binding.layoutFilterInfoSection.tvSort.text = sortType.title
                        }
                }

                launch {
                    viewModel.filterUiState
                        .filterIsInstance<FilterUiState.Success>()
                        .map { it.filters.categoryFilters }
                        .distinctUntilChanged()
                        .collect { categories ->
                            categoryListAdapter.submitList(categories)
                        }
                }

                launch {
                    viewModel.filterUiState
                        .filterIsInstance<FilterUiState.Success>()
                        .map { it.filters.paymentMethodFilters }
                        .distinctUntilChanged()
                        .collect { paymentMethods ->
                            paymentMethodListAdapter.submitList(paymentMethods)
                        }
                }

                launch {
                    viewModel.filterUiState
                        .filterIsInstance<FilterUiState.Success>()
                        .map { it.filters.purchaseNameFilter }
                        .distinctUntilChanged()
                        .collect(::updatePurchaseNameFilterUi)
                }

                launch {
                    viewModel.filterUiState
                        .filterIsInstance<FilterUiState.Success>()
                        .map { it.filters.priceFilter }
                        .distinctUntilChanged()
                        .collect(::updatePriceFilterUi)
                }

                launch {
                    viewModel.appliedFilterItems.collect {
                        with (binding) {
                            clSelectedFilters.isVisible = it.isNotEmpty()
                            layoutFilterInfoSection.ivNoti.isVisible = it.isNotEmpty()
                        }
                        selectedFilterListAdapter.submitList(it)
                    }
                }

                launch {
                    viewModel.filteredPurchaseNotesUiState.collect { notesUiState ->
                        when (notesUiState) {
                            is FilteredPurchaseNotesUiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is FilteredPurchaseNotesUiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                                showToast(notesUiState.errorMsg)
                            }
                            is FilteredPurchaseNotesUiState.PurchaseNotes -> {
                                binding.layoutLoading.root.makeGone()
                                binding.layoutFilterInfoSection.tvResultsCount.text =
                                    getString(R.string.format_total_count, notesUiState.totalCount)

                                resultListAdapter.submitList(notesUiState.items) {
                                    if (viewModel.shouldScrollToTop) {
                                        binding.rvSearchResults.scrollToPosition(0)
                                        viewModel.shouldScrollToTop = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updatePurchaseNameFilterUi(purchaseNameFilter: Filters.PurchaseName) =
        with(binding.layoutFilterContainer) {
            tvPurchaseName.text = purchaseNameFilter.purchaseName

            if (purchaseNameFilter.isApplied()) {
                tvPurchaseName.setTextColorRes(appliedTextColor)
                tvPurchaseName.setTypeface(null, Typeface.BOLD)
            } else {
                tvPurchaseName.setTextColorRes(normalTextColor)
                tvPurchaseName.setTypeface(null, Typeface.NORMAL)
            }
        }

    private fun updatePriceFilterUi(priceFilter: Filters.Price) =
        with(binding.layoutFilterContainer) {
            if (priceFilter.isApplied()) {
                tvPrice.setTypeface(null, Typeface.BOLD)
                tvPrice.setTextColorRes(appliedTextColor)
            } else {
                tvPrice.setTypeface(null, Typeface.NORMAL)
                tvPrice.setTextColorRes(normalTextColor)
            }

            val (minPrice, maxPrice, myMaxPrice) = priceFilter

            tvPrice.text = if (minPrice == null && maxPrice != null) {
                getString(R.string.format_won_below, maxPrice.toComma())
            } else if (minPrice != null && maxPrice == null) {
                getString(R.string.format_won_up, minPrice.toComma())
            } else {
                getString(
                    R.string.format_min_price_until_max_price,
                    minPrice?.toComma() ?: "0",
                    maxPrice?.toComma() ?: myMaxPrice.toComma()
                )
            }
        }

    private val categoryFilterClickAction: (Int) -> Unit = { categoryId ->
        viewModel.addOrDeleteCategoryItemBy(categoryId)
    }

    private val paymentMethodFilterClickAction: (Int) -> Unit = { paymentMethodId ->
        viewModel.addOrDeletePaymentMethodItemBy(paymentMethodId)
    }

    private val selectedFilterClickAction: (Filters) -> Unit = { filter ->
        viewModel.deleteFilter(filter)
    }

    private val backButtonClickListener = View.OnClickListener {
        finish()
    }

    private val purchaseNameFilterClickListener = View.OnClickListener {
        PurchaseNotePurchaseNameBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNotePurchaseNameBSDialog.TAG)
    }

    private val dateClickListener = View.OnClickListener {
        PurchaseNoteDatePeriodFilterBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNoteDatePeriodFilterBSDialog.TAG)
    }

    private val priceFilterClickListener = View.OnClickListener {
        PurchaseNotePriceFilterBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNotePriceFilterBSDialog.TAG)
    }

    private val sortFilterClickListener = View.OnClickListener {
        val filterUiState = viewModel.filterUiState.value as? FilterUiState.Success ?: return@OnClickListener
        val currentSortType = filterUiState.filters.sortType
        val sortItem = SortType.entries.map { sortType ->
            SortItem(
                type = sortType,
                isSelected = sortType == currentSortType
            )
        }

        SortBSDialog.newInstance(sortItem)
            .show(supportFragmentManager, SortBSDialog.TAG)
    }

    private val conditionFilterClickListener = View.OnClickListener {
        PurchaseNoteSearchWholeFilterBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNoteSearchWholeFilterBSDialog.TAG)
    }

    private val selectedFilterResetBtnClickListener = View.OnClickListener {
        viewModel.resetSelectedFilters()
    }

    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit = { purchaseNoteItem ->
        Intent(this, PurchaseNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PURCHASE_NOTE_ID, purchaseNoteItem.id)
            startActivity(this)
        }
    }

    override fun onClickSort(sort: SortType) {
        viewModel.setSortType(sort)
    }

    companion object {
        private val appliedTextColor = R.color.colorPrimary
        private val normalTextColor = R.color.black_500
    }
}
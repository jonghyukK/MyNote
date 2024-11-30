package com.kjh.mynote.ui.features.purchase.search

import android.content.Intent
import android.graphics.Typeface
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPurchaseNoteSearchBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.purchase.detail.PurchaseNoteDetailActivity
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchResultListAdapter
import com.kjh.mynote.ui.features.purchase.search.adapter.PurchaseNoteSearchSelectedFilterListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.category.PurchaseNoteSearchCategoryListAdapter
import com.kjh.mynote.ui.features.purchase.search.filters.date.PurchaseNoteDatePeriodFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.price.PurchaseNotePriceFilterBSDialog
import com.kjh.mynote.ui.features.purchase.search.filters.purchasename.PurchaseNotePurchaseNameBSDialog
import com.kjh.mynote.utils.PurchaseNoteSearchItemDecoration
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setBackgroundRes
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toComma
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

@AndroidEntryPoint
class PurchaseNoteSearchActivity: BaseActivity<ActivityPurchaseNoteSearchBinding>({ ActivityPurchaseNoteSearchBinding.inflate(it) }) {

    private val viewModel: PurchaseNoteSearchViewModel by viewModels()

    private val categoryListAdapter: PurchaseNoteSearchCategoryListAdapter by lazy {
        PurchaseNoteSearchCategoryListAdapter(categoryFilterClickAction)
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

            tvPurchaseName.setOnThrottleClickListener(purchaseNameFilterClickListener)
            tvPrice.setOnThrottleClickListener(priceFilterClickListener)
        }

        with (binding.layoutFilterInfoSection) {
            clConditionFilter.setOnThrottleClickListener(conditionFilterClickListener)
        }

        with (binding) {
            rvSearchResults.apply {
                itemAnimator = null
                addItemDecoration(PurchaseNoteSearchItemDecoration())
                adapter = resultListAdapter
            }

            rvSelectedFilters.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(right = 8, exceptFirstItem = false))
                adapter = selectedFilterListAdapter
            }

            llDateContainer.setOnThrottleClickListener(dateClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.filtersUiState
                        .map { it.dateRangeFilter }
                        .distinctUntilChanged()
                        .collect { dateFilter ->
                            when (dateFilter.dateRangeFilter) {
                                is DateRangeFilter.Monthly,
                                is DateRangeFilter.MonthOne,
                                is DateRangeFilter.MonthThree,
                                is DateRangeFilter.Directly -> {
                                    binding.tvDate.text = dateFilter.dateRangeFilter.getUiText()
                                }
                                null -> {
                                    binding.tvDate.text = getString(R.string.recent_years_ago)
                                }
                            }
                        }
                }

                launch {
                    viewModel.resultTotalCount.collect { resultTotalCount ->
                        binding.layoutFilterInfoSection.tvResultsCount.text =
                            getString(R.string.format_total_count, resultTotalCount)
                    }
                }

                launch {
                    viewModel.uiState.collect { uiList ->
                        resultListAdapter.submitList(uiList)
                    }
                }

                launch {
                    viewModel.appliedFilterItem.collect {
                        binding.clSelectedFilters.isVisible = it.isNotEmpty()
                        selectedFilterListAdapter.submitList(it)
                    }
                }

                launch {
                    viewModel.filtersUiState.collect { item ->
                        val appliedTextColor = R.color.purple
                        val normalTextColor = R.color.black_500

                        with (binding.layoutFilterContainer) {
                            // 카테고리 ..
                            categoryListAdapter.submitList(item.categoryFilters)

                            with (binding) {
                                // 구매명 ..
                                tvPurchaseName.text = item.purchaseNameFilter.purchaseName

                                if (item.purchaseNameFilter.isApplied()) {
                                    tvPurchaseName.setTextColorRes(appliedTextColor)
                                    tvPurchaseName.setBackgroundRes(R.drawable.ripple_shape_s_white_c_4_l_purple)
                                } else {
                                    tvPurchaseName.setTextColorRes(normalTextColor)
                                    tvPurchaseName.setBackgroundRes(R.drawable.ripple_shape_s_white_c_4_l_black_500)
                                }

                                // 가격 ..
                                if (item.priceFilter.isApplied()) {
                                    tvPrice.setTypeface(null, Typeface.BOLD)
                                    tvPrice.setTextColorRes(appliedTextColor)
                                } else {
                                    tvPrice.setTypeface(null, Typeface.NORMAL)
                                    tvPrice.setTextColorRes(normalTextColor)
                                }

                                val (minPrice, maxPrice, myMaxPrice) = item.priceFilter
                                if (minPrice == null && maxPrice != null) {
                                    tvPrice.text =
                                        getString(R.string.format_won_below, maxPrice.toComma())
                                } else if (minPrice != null && maxPrice == null) {
                                    tvPrice.text =
                                        getString(R.string.format_won_up, minPrice.toComma())
                                } else {
                                    tvPrice.text = getString(
                                        R.string.format_min_price_until_max_price,
                                        minPrice?.toComma() ?: "0",
                                        maxPrice?.toComma() ?: myMaxPrice.toComma()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val categoryFilterClickAction: (Int) -> Unit = { categoryId ->
        viewModel.addOrDeleteCategoryItemBy(categoryId)
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

    private val conditionFilterClickListener = View.OnClickListener {
        PurchaseNoteSearchFilterBSDialog.newInstance()
            .show(supportFragmentManager, PurchaseNoteSearchFilterBSDialog.TAG)
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
package com.kjh.mynote.ui.features.purchase.search.adapter

import android.graphics.Typeface
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptySearchResultsBinding
import com.kjh.mynote.databinding.LayoutLoadingBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchFilterItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchSelectedFilterOuterBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.purchase.search.Filters
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchFilterUiState
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchUiState
import com.kjh.mynote.ui.features.purchase.search.filters.category.PurchaseNoteSearchCategoryListAdapter
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setBackgroundRes
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.setTint
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */

/**
 * 구매노트 검색 로딩 ViewHolder.
 *
 * @property binding
 */
class PurchaseNoteSearchLoadingItemViewHolder(
    private val binding: LayoutLoadingBinding
): BaseViewHolder<Unit>(binding.root) {

    override fun bind(item: Unit) {
        super.bind(item)
        binding.root.setBackgroundResource(R.color.transparent)
    }
}

/**
 * 구매노트 검색 빈 화면 ViewHolder.
 *
 * @property binding
 */
class PurchaseNoteSearchEmptyItemViewHolder(
    private val binding: LayoutEmptySearchResultsBinding
): BaseViewHolder<Unit>(binding.root) {}

class PurchaseNoteSearchDateItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultDateItemBinding
): BaseViewHolder<PurchaseNoteSearchUiState.DateItem>(binding.root) {

    override fun bind(item: PurchaseNoteSearchUiState.DateItem) {
        super.bind(item)

        binding.tvDate.text = item.date.toStringWithPattern("yyyy년 M월 d일 (E)")
    }
}

/**
 *  구매노트 검색 필터 화면 ViewHolder.
 *
 * @property binding
 * @property categoryClickAction
 * @property purchaseNameClickAction
 * @property dateFilterClickAction
 * @property priceFilterClickAction
 */
class PurchaseNoteSearchFilterItemViewHolder(
    private val binding: VhPurchaseNoteSearchFilterItemBinding,
    private val categoryClickAction: (Int) -> Unit,
    private val purchaseNameClickAction: () -> Unit,
    private val dateFilterClickAction: () -> Unit,
    private val priceFilterClickAction: () -> Unit
): BaseViewHolder<PurchaseNoteSearchFilterUiState>(binding.root) {

    private val categoryListAdapter =
        PurchaseNoteSearchCategoryListAdapter(categoryClickAction)

    init {
        binding.rvCategories.apply {
            itemAnimator = null
            adapter = categoryListAdapter
        }

        binding.tvPurchaseName.onThrottleClick {
            bindItem?.let { purchaseNameClickAction.invoke() }
        }

        binding.clDateContainer.onThrottleClick {
            bindItem?.let { dateFilterClickAction.invoke() }
        }

        binding.tvPrice.onThrottleClick {
            bindItem?.let { priceFilterClickAction.invoke() }
        }
    }

    override fun bind(item: PurchaseNoteSearchFilterUiState) {
        super.bind(item)

        // 카테고리 ..
        categoryListAdapter.submitList(item.categoryFilters)

        with (binding) {
            // 구매명 ..
            tvPurchaseName.text = item.purchaseNameFilter.purchaseName
            if (item.purchaseNameFilter.purchaseName.isNotEmpty()) {
                tvPurchaseName.setTextColorRes(appliedTextColor)
                tvPurchaseName.setBackgroundRes(R.drawable.ripple_shape_s_white_c_4_l_purple)
            } else {
                tvPurchaseName.setTextColorRes(normalTextColor)
                tvPurchaseName.setBackgroundRes(R.drawable.ripple_shape_s_white_c_4_l_black_500)
            }

            // 조회 기간..
            when (val dateRange = item.dateRangeFilter.dateRangeFilter) {
                is DateRangeFilter.Monthly -> {
                    ivDateCalendar.setTint(appliedTextColor)
                    tvDate.setTextColorRes(appliedTextColor)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = dateRange.getUiText()
                }
                is DateRangeFilter.MonthOne -> {
                    ivDateCalendar.setTint(appliedTextColor)
                    tvDate.setTextColorRes(appliedTextColor)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = dateRange.getUiText()
                }
                is DateRangeFilter.MonthThree -> {
                    ivDateCalendar.setTint(appliedTextColor)
                    tvDate.setTextColorRes(appliedTextColor)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = dateRange.getUiText()
                }
                is DateRangeFilter.Directly -> {
                    ivDateCalendar.setTint(appliedTextColor)
                    tvDate.setTextColorRes(appliedTextColor)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = dateRange.getUiText()
                }
                null -> {
                    ivDateCalendar.setTint(normalTextColor)
                    tvDate.setTextColorRes(normalTextColor)
                    tvDate.setTypeface(null, Typeface.NORMAL)
                    tvDate.text = context.getString(R.string.recent_years_ago)
                }
            }

            // 가격 ..
            if (item.priceFilter.isApplied) {
                tvPrice.setTypeface(null, Typeface.BOLD)
                tvPrice.setTextColorRes(appliedTextColor)
            } else {
                tvPrice.setTypeface(null, Typeface.NORMAL)
                tvPrice.setTextColorRes(normalTextColor)
            }

            tvPrice.text = context.getString(R.string.format_min_price_until_max_price,
                item.priceFilter.minPrice.toComma(), item.priceFilter.maxPrice.toComma())
        }
    }

    companion object {
        private val appliedTextColor = R.color.purple
        private val normalTextColor = R.color.black_500
    }
}

/**
 * 구매노트 검색 선택된 필터 목록 화면 ViewHolder.
 *
 * @property binding
 * @property filterClickAction
 */
class PurchaseNoteSearchSelectedFilterOuterItemViewHolder(
    private val binding: VhPurchaseNoteSearchSelectedFilterOuterBinding,
    private val filterClickAction: (Filters) -> Unit
): BaseViewHolder<PurchaseNoteSearchUiState.AppliedFilterItems>(binding.root) {

    private val selectedFilterListAdapter = PurchaseNoteSearchSelectedFilterListAdapter(filterClickAction)

    init {
        binding.rvSelectedFilters.apply {
            itemAnimator = null
            adapter = selectedFilterListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(right = 8, exceptFirstItem = false))
            }
        }
    }

    override fun bind(item: PurchaseNoteSearchUiState.AppliedFilterItems) {
        super.bind(item)

        selectedFilterListAdapter.submitList(item.filterItems)
    }
}

/**
 * 구매노트 검색 결과 항목 ViewHolder.
 *
 * @property binding
 * @property onClickAction
 */
class PurchaseNoteSearchResultItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultItemBinding,
    private val onClickAction: (PurchaseNoteUiModel) -> Unit
): BaseViewHolder<PurchaseNoteSearchUiState.ResultItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onClickAction.invoke(item.purchaseNoteItem) }
        }
    }

    override fun bind(item: PurchaseNoteSearchUiState.ResultItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.purchaseNoteItem.category?.categoryName ?: "카레고리 없음"
            tvPurchaseName.text = item.purchaseNoteItem.purchaseName
            tvPrice.text = context.getString(R.string.format_won, item.purchaseNoteItem.purchasePrice.toComma())
        }
    }
}
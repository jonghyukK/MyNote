package com.kjh.mynote.ui.features.purchase.search.adapter

import android.graphics.Typeface
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptySearchResultsBinding
import com.kjh.mynote.databinding.LayoutLoadingBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchFilterItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchFilterUiState
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchUiState
import com.kjh.mynote.ui.features.purchase.search.filters.category.PurchaseNoteSearchCategoryListAdapter
import com.kjh.mynote.ui.features.purchase.search.isChanged
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */


class PurchaseNoteSearchLoadingItemViewHolder(
    private val binding: LayoutLoadingBinding
): BaseViewHolder<Unit>(binding.root) {

    override fun bind(item: Unit) {
        super.bind(item)
        binding.root.setBackgroundResource(R.color.transparent)
    }
}

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
            addItemDecoration(SpacingItemDecoration(left = 6))
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
        categoryListAdapter.submitList(item.categoryItems)

        with (binding) {
            tvPurchaseName.text = item.purchaseName

            // 조회 기간..
            when (val dateRange = item.dateRangeFilter) {
                is DateRangeFilter.Monthly -> {
                    tvDate.setTextColorRes(R.color.purple)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = dateRange.getDateUiText()
                }
                is DateRangeFilter.MonthOne -> {
                    tvDate.setTextColorRes(R.color.purple)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = context.getString(
                        R.string.format_start_date_until_end_date,
                        dateRange.getStartDateUiText(), dateRange.getEndDateUiText())
                }
                is DateRangeFilter.MonthThree -> {
                    tvDate.setTextColorRes(R.color.purple)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = context.getString(
                        R.string.format_start_date_until_end_date,
                        dateRange.getStartDateUiText(), dateRange.getEndDateUiText())
                }
                is DateRangeFilter.Directly -> {
                    tvDate.setTextColorRes(R.color.purple)
                    tvDate.setTypeface(null, Typeface.BOLD)
                    tvDate.text = context.getString(
                        R.string.format_start_date_until_end_date,
                        dateRange.getStartDateUiText(), dateRange.getEndDateUiText())
                }
                null -> {
                    tvDate.setTextColorRes(R.color.black_500)
                    tvDate.setTypeface(null, Typeface.NORMAL)
                    tvDate.text = context.getString(R.string.recent_years_ago)
                }
            }

            // 가격 ..
            if (item.priceFilter.isChanged()) {
                tvPrice.setTypeface(null, Typeface.BOLD)
                tvPrice.setTextColorRes(R.color.purple)
            } else {
                tvPrice.setTypeface(null, Typeface.NORMAL)
                tvPrice.setTextColorRes(R.color.black_500)
            }

            tvPrice.text = context.getString(R.string.format_min_price_until_max_price,
                item.priceFilter.minPrice.toComma(), item.priceFilter.maxPrice.toComma())
        }

    }
}

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
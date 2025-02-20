package com.kjh.mynote.ui.features.statistics.category.adapter.total

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhCategoryPurchaseNotesStatsInfoItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.category.CategoryStatisticsUiItem
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */

class TotalStatsSectionItemViewHolder(
    private val binding: VhCategoryPurchaseNotesStatsInfoItemBinding,
    private val categoryClickAction: () -> Unit
): BaseViewHolder<CategoryStatisticsUiItem.StatsInfo>(binding.root) {

    init {
        binding.clCategory.onThrottleClick {
            bindItem?.let { categoryClickAction() }
        }
    }

    override fun bind(item: CategoryStatisticsUiItem.StatsInfo) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.currentCategory.categoryName
            tvTotalNoteCount.highlightText(
                fullText = context.getString(R.string.format_total_count_with_unit, item.totalNoteCount),
                wordToHighlight = item.totalNoteCount.toString(),
                true
            )

            tvTotalNotePrice.highlightText(
                fullText = context.getString(R.string.format_total_price, item.totalNotePrice.toComma()),
                wordToHighlight = item.totalNotePrice.toComma(),
                true
            )
        }
    }
}
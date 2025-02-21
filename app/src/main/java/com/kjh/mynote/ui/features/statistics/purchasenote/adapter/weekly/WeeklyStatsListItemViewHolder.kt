package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.weekly

import com.kjh.mynote.databinding.VhPurchaseNoteStatsWeeklyListItemBinding
import com.kjh.mynote.model.WeeklyPurchaseNoteStatsUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
class WeeklyStatsListItemViewHolder(
    private val binding: VhPurchaseNoteStatsWeeklyListItemBinding
): BaseViewHolder<WeeklyPurchaseNoteStatsUiModel>(binding.root) {

    override fun bind(item: WeeklyPurchaseNoteStatsUiModel) {
        super.bind(item)

        with (binding) {
            tvWeekLabel.text = item.weekLabel
            tvDateRange.text = item.dateFormatted
            tvPriceCount.text = "${item.totalPrice.toComma()}원 / ${item.totalCount}건"
        }
    }
}
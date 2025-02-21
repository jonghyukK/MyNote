package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.weekly

import com.kjh.mynote.databinding.VhPurchaseNoteStatsWeeklySectionItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
class WeeklyStatsSectionItemViewHolder(
    private val binding: VhPurchaseNoteStatsWeeklySectionItemBinding
): BaseViewHolder<PurchaseNoteStatisticsUiItemState.WeeklyStatsSection>(binding.root) {

    private val innerListAdapter: WeeklyStatsListAdapter = WeeklyStatsListAdapter()

    init {
        binding.rvWeeklyList.apply {
            adapter = innerListAdapter
        }
    }

    override fun bind(item: PurchaseNoteStatisticsUiItemState.WeeklyStatsSection) {
        super.bind(item)

        binding.tvTotalStats.text = "${item.totalPrice.toComma()}원 / ${item.totalCount}건"
        innerListAdapter.submitList(item.childItems)
    }
}
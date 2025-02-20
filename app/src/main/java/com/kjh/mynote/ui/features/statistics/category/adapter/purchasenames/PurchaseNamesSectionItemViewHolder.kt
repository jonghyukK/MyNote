package com.kjh.mynote.ui.features.statistics.category.adapter.purchasenames

import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsRankingSectionItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.category.CategoryStatisticsUiItem
import com.kjh.mynote.utils.decorations.SpacingItemDecoration

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */
class PurchaseNamesSectionItemViewHolder(
    private val binding: VhCategoryPurchaseNoteStatsRankingSectionItemBinding
): BaseViewHolder<CategoryStatisticsUiItem.PurchaseNameStats>(binding.root) {

    private val innerListAdapter = PurchaseNameListAdapter()

    init {
        binding.rvRankings.apply {
            itemAnimator = null
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(top = 6))
            }
            adapter = innerListAdapter
        }
    }

    override fun bind(item: CategoryStatisticsUiItem.PurchaseNameStats) {
        super.bind(item)
        innerListAdapter.submitList(item.purchaseNameStatsItems)
    }
}
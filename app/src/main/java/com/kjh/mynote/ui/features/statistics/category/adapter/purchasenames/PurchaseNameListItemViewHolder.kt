package com.kjh.mynote.ui.features.statistics.category.adapter.purchasenames

import androidx.core.content.ContextCompat
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.category.PurchaseNameStatsItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 18..
 * Description:
 */

class PurchaseNameListItemViewHolder(
    private val binding: VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding
): BaseViewHolder<PurchaseNameStatsItem>(binding.root) {

    override fun bind(item: PurchaseNameStatsItem) {
        super.bind(item)

        with (binding) {
            tvPurchaseName.text = item.purchaseNameStats.purchaseName
            tvCount.text = "${item.purchaseNameStats.totalCount}건"

            lpiProgressBar.setIndicatorColor(ContextCompat.getColor(context, item.color))
            val progress = item.purchaseNameStats.totalCount
            lpiProgressBar.max = item.maxCount
            lpiProgressBar.setProgress(progress, false)
        }
    }
}
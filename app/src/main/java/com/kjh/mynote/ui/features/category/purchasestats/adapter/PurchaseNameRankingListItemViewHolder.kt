package com.kjh.mynote.ui.features.category.purchasestats.adapter

import androidx.core.content.ContextCompat
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.category.purchasestats.PurchaseNameStatsItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 18..
 * Description:
 */

class PurchaseNameRankingListItemViewHolder(
    private val binding: VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding
): BaseViewHolder<PurchaseNameStatsItem>(binding.root) {

    override fun bind(item: PurchaseNameStatsItem) {
        super.bind(item)

        with (binding) {
            tvPurchaseName.text = item.purchaseName
            tvCount.text = "${item.totalCount}건"

            lpiProgressBar.setIndicatorColor(ContextCompat.getColor(context, item.color))
            val progress = item.totalCount
            lpiProgressBar.max = item.maxCount
            lpiProgressBar.setProgress(progress, false)
        }
    }
}
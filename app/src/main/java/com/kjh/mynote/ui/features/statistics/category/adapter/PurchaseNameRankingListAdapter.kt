package com.kjh.mynote.ui.features.statistics.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding
import com.kjh.mynote.ui.features.statistics.category.PurchaseNameStatsItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 18..
 * Description:
 */

class PurchaseNameRankingListAdapter(

): ListAdapter<PurchaseNameStatsItem, PurchaseNameRankingListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseNameRankingListItemViewHolder(
            VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PurchaseNameRankingListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNameStatsItem>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNameStatsItem,
                    newItem: PurchaseNameStatsItem,
                ): Boolean = oldItem.purchaseName == newItem.purchaseName

                override fun areContentsTheSame(
                    oldItem: PurchaseNameStatsItem,
                    newItem: PurchaseNameStatsItem,
                ): Boolean = oldItem == newItem
            }
    }
}
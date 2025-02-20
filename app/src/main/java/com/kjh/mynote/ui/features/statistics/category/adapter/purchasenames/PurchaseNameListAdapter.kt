package com.kjh.mynote.ui.features.statistics.category.adapter.purchasenames

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

class PurchaseNameListAdapter
    : ListAdapter<PurchaseNameStatsItem, PurchaseNameListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseNameListItemViewHolder(
            VhCategoryPurchaseNoteStatsPurchaseNameRankingItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: PurchaseNameListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNameStatsItem>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNameStatsItem,
                    newItem: PurchaseNameStatsItem,
                ): Boolean = oldItem.purchaseNameStats.purchaseName == newItem.purchaseNameStats.purchaseName

                override fun areContentsTheSame(
                    oldItem: PurchaseNameStatsItem,
                    newItem: PurchaseNameStatsItem,
                ): Boolean = oldItem == newItem
            }
    }
}
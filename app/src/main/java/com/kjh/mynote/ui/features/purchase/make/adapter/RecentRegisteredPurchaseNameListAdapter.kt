package com.kjh.mynote.ui.features.purchase.make.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhRecentRegisteredPurchaseNameItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 18..
 * Description:
 */
class RecentRegisteredPurchaseNameListAdapter(
    private val recentPurchaseNameClickAction: (String) -> Unit
): ListAdapter<String, RecentRegisteredPurchaseNameItemViewHolder>(UI_MODEL_COMPARATOR) {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = RecentRegisteredPurchaseNameItemViewHolder(
        VhRecentRegisteredPurchaseNameItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), recentPurchaseNameClickAction
    )

    override fun onBindViewHolder(
        holder: RecentRegisteredPurchaseNameItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem
        }
    }
}
package com.kjh.mynote.ui.features.purchase.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteDetailImageItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class PurchaseNoteDetailImageListAdapter(
    private val imageClickAction: (String) -> Unit
): ListAdapter<String, PurchaseNoteDetailImageItemViewHolder>(UI_MODEL_COMPARATOR) {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PurchaseNoteDetailImageItemViewHolder(
        VhPurchaseNoteDetailImageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), imageClickAction
    )

    override fun onBindViewHolder(holder: PurchaseNoteDetailImageItemViewHolder, position: Int) {
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
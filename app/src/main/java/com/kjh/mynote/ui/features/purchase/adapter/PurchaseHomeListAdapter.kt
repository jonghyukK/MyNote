package com.kjh.mynote.ui.features.purchase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteHomeListItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class PurchaseHomeListAdapter
    : ListAdapter<PurchaseNoteUiModel, PurchaseNoteHomeListItemViewHolder>(UI_MODEL_COMPARATOR) {
        
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PurchaseNoteHomeListItemViewHolder(
        VhPurchaseNoteHomeListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: PurchaseNoteHomeListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PurchaseNoteUiModel>() {
            override fun areItemsTheSame(
                oldItem: PurchaseNoteUiModel,
                newItem: PurchaseNoteUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PurchaseNoteUiModel,
                newItem: PurchaseNoteUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
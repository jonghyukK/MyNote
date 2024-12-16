package com.kjh.mynote.ui.features.purchase.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.common.vh.PurchaseNoteItemViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class PurchaseHomeListAdapter(
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
) : ListAdapter<PurchaseNoteUiModel, PurchaseNoteItemViewHolder>(UI_MODEL_COMPARATOR) {
        
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PurchaseNoteItemViewHolder(
        VhPurchaseNoteSearchResultItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), purchaseNoteItemClickAction
    )

    override fun onBindViewHolder(holder: PurchaseNoteItemViewHolder, position: Int) {
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
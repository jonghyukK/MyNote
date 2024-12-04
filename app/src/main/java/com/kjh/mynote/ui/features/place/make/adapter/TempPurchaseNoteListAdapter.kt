package com.kjh.mynote.ui.features.place.make.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhAddTempPurchaseNoteItemBinding
import com.kjh.mynote.ui.features.place.make.TempPurchaseNoteItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */
class TempPurchaseNoteListAdapter(
    private val tempPurchaseNoteItemClickAction: (TempPurchaseNoteItem) -> Unit,
    private val removePurchaseNoteBtnClickAction: (TempPurchaseNoteItem) -> Unit
): ListAdapter<TempPurchaseNoteItem, TempPurchaseNoteItemViewHolder>(UI_MODEL_COMPARATOR) {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = TempPurchaseNoteItemViewHolder(
        VhAddTempPurchaseNoteItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), tempPurchaseNoteItemClickAction, removePurchaseNoteBtnClickAction
    )

    override fun onBindViewHolder(holder: TempPurchaseNoteItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<TempPurchaseNoteItem>() {
                override fun areItemsTheSame(
                    oldItem: TempPurchaseNoteItem,
                    newItem: TempPurchaseNoteItem
                ): Boolean = oldItem.tempId == newItem.tempId

                override fun areContentsTheSame(
                    oldItem: TempPurchaseNoteItem,
                    newItem: TempPurchaseNoteItem
                ): Boolean = oldItem == newItem
            }
    }
}
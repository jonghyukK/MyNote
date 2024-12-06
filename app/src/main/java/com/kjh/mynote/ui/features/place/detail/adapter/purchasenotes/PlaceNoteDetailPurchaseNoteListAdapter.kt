package com.kjh.mynote.ui.features.place.detail.adapter.purchasenotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 6..
 * Description:
 */
class PlaceNoteDetailPurchaseNoteListAdapter(
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
): ListAdapter<PurchaseNoteUiModel, PlaceNoteDetailPurchaseNoteItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PlaceNoteDetailPurchaseNoteItemViewHolder(
        VhPurchaseNoteSearchResultItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), purchaseNoteItemClickAction
    )

    override fun onBindViewHolder(
        holder: PlaceNoteDetailPurchaseNoteItemViewHolder,
        position: Int,
    ) {
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
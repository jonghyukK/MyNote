package com.kjh.mynote.ui.features.purchase.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.common.uistate.PurchaseNotesUiState
import com.kjh.mynote.ui.common.vh.PurchaseNoteDateItemViewHolder
import com.kjh.mynote.ui.common.vh.PurchaseNoteItemViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */
class PurchaseNoteSearchResultListAdapter(
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
): ListAdapter<PurchaseNotesUiState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        // 날짜 아이템.
        R.layout.vh_purchase_note_search_result_date_item -> {
            PurchaseNoteDateItemViewHolder(
                VhPurchaseNoteSearchResultDateItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // 결과 아이템.
        R.layout.vh_purchase_note_search_result_item -> {
            PurchaseNoteItemViewHolder(
                VhPurchaseNoteSearchResultItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }

        else -> throw Exception("wrong viewType: $viewType")
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is PurchaseNotesUiState.PurchaseNoteItem -> {
                (holder as PurchaseNoteItemViewHolder).bind(item.purchaseNote)
            }
            is PurchaseNotesUiState.DateItem -> {
                (holder as PurchaseNoteDateItemViewHolder).bind(item.date)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNotesUiState.DateItem -> R.layout.vh_purchase_note_search_result_date_item
        is PurchaseNotesUiState.PurchaseNoteItem -> R.layout.vh_purchase_note_search_result_item
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNotesUiState>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNotesUiState,
                    newItem: PurchaseNotesUiState
                ): Boolean = when {
                        oldItem is PurchaseNotesUiState.DateItem
                                && newItem is PurchaseNotesUiState.DateItem -> {
                            oldItem.date == newItem.date
                        }

                        oldItem is PurchaseNotesUiState.PurchaseNoteItem
                                && newItem is PurchaseNotesUiState.PurchaseNoteItem -> {
                            oldItem.purchaseNote.id == newItem.purchaseNote.id
                        }
                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: PurchaseNotesUiState,
                    newItem: PurchaseNotesUiState
                ): Boolean = oldItem == newItem
            }
    }
}
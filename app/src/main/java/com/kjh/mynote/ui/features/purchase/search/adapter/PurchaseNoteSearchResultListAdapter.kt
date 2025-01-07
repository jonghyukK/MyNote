package com.kjh.mynote.ui.features.purchase.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptyMyPurchasesBinding
import com.kjh.mynote.databinding.LayoutEmptySearchResultsBinding
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
        VIEW_TYPE_DATE -> {
            PurchaseNoteDateItemViewHolder(
                VhPurchaseNoteSearchResultDateItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // 결과 아이템.
        VIEW_TYPE_PURCHASE_NOTE -> {
            PurchaseNoteItemViewHolder(
                VhPurchaseNoteSearchResultItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }
        // Empty.
        VIEW_TYPE_EMPTY -> {
            PurchaseNoteSearchResultEmptyViewHolder(
                LayoutEmptySearchResultsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
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
            is PurchaseNotesUiState.Empty -> {
                (holder as PurchaseNoteSearchResultEmptyViewHolder).bind(Unit)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNotesUiState.DateItem -> VIEW_TYPE_DATE
        is PurchaseNotesUiState.PurchaseNoteItem -> VIEW_TYPE_PURCHASE_NOTE
        is PurchaseNotesUiState.Empty -> VIEW_TYPE_EMPTY
    }

    companion object {
        private const val VIEW_TYPE_DATE = 1
        private const val VIEW_TYPE_PURCHASE_NOTE = 2
        private const val VIEW_TYPE_EMPTY = 3

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNotesUiState>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNotesUiState,
                    newItem: PurchaseNotesUiState,
                ): Boolean = when {
                    oldItem is PurchaseNotesUiState.DateItem
                            && newItem is PurchaseNotesUiState.DateItem -> {
                        oldItem.date == newItem.date
                    }

                    oldItem is PurchaseNotesUiState.PurchaseNoteItem
                            && newItem is PurchaseNotesUiState.PurchaseNoteItem -> {
                        oldItem.purchaseNote.id == newItem.purchaseNote.id
                    }

                    oldItem is PurchaseNotesUiState.Empty && newItem is PurchaseNotesUiState.Empty -> {
                        true
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
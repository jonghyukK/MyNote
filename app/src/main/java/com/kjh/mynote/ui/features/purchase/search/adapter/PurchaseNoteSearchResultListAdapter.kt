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
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchResultItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */
class PurchaseNoteSearchResultListAdapter(
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
): ListAdapter<PurchaseNoteSearchResultItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        // 날짜 아이템.
        R.layout.vh_purchase_note_search_result_date_item -> {
            PurchaseNoteSearchDateItemViewHolder(
                VhPurchaseNoteSearchResultDateItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // 결과 아이템.
        R.layout.vh_purchase_note_search_result_item -> {
            PurchaseNoteSearchResultItemViewHolder(
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
            is PurchaseNoteSearchResultItem.ResultItem -> {
                (holder as PurchaseNoteSearchResultItemViewHolder).bind(item)
            }
            is PurchaseNoteSearchResultItem.DateItem -> {
                (holder as PurchaseNoteSearchDateItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNoteSearchResultItem.DateItem -> R.layout.vh_purchase_note_search_result_date_item
        is PurchaseNoteSearchResultItem.ResultItem -> R.layout.vh_purchase_note_search_result_item
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNoteSearchResultItem>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNoteSearchResultItem,
                    newItem: PurchaseNoteSearchResultItem
                ): Boolean = when {
                        oldItem is PurchaseNoteSearchResultItem.DateItem
                                && newItem is PurchaseNoteSearchResultItem.DateItem -> {
                            oldItem.date == newItem.date
                        }

                        oldItem is PurchaseNoteSearchResultItem.ResultItem
                                && newItem is PurchaseNoteSearchResultItem.ResultItem -> {
                            oldItem.purchaseNoteItem.id == newItem.purchaseNoteItem.id
                        }
                        else -> false
                    }

                override fun areContentsTheSame(
                    oldItem: PurchaseNoteSearchResultItem,
                    newItem: PurchaseNoteSearchResultItem
                ): Boolean = oldItem == newItem
            }
    }
}
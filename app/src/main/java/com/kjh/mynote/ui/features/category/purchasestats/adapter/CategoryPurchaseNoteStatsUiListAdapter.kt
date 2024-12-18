package com.kjh.mynote.ui.features.category.purchasestats.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.LayoutEmptyMyPurchasesBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsRankingSectionItemBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNotesStatsInfoItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.common.vh.PurchaseNoteDateItemViewHolder
import com.kjh.mynote.ui.common.vh.PurchaseNoteItemViewHolder
import com.kjh.mynote.ui.features.category.purchasestats.CategoryPurchaseNoteStatsUiItems

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

class CategoryPurchaseNoteStatsUiListAdapter(
    private val categoryClickAction: () -> Unit,
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit,
    private val makePurchaseNoteClickAction: () -> Unit
): ListAdapter<CategoryPurchaseNoteStatsUiItems, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_STATS_INFO -> {
            CategoryPurchaseNoteStatsInfoItemViewHolder(
                VhCategoryPurchaseNotesStatsInfoItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), categoryClickAction
            )
        }
        VIEW_TYPE_PURCHASE_NAME_RANKING -> {
            CategoryPurchaseNoteStatsRankingSectionItemViewHolder(
                VhCategoryPurchaseNoteStatsRankingSectionItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        VIEW_TYPE_PURCHASE_NOTE_DATE -> {
            PurchaseNoteDateItemViewHolder(
                VhPurchaseNoteSearchResultDateItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        VIEW_TYPE_PURCHASE_NOTE -> {
            PurchaseNoteItemViewHolder(
                VhPurchaseNoteSearchResultItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }

        VIEW_TYPE_EMPTY -> {
            PurchaseNotesEmptyItemViewHolder(
                LayoutEmptyMyPurchasesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), makePurchaseNoteClickAction
            )
        }

        else -> throw IllegalArgumentException("wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryPurchaseNoteStatsUiItems.Empty -> {
                (holder as PurchaseNotesEmptyItemViewHolder).bind(Unit)
            }
            is CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem -> {
                (holder as CategoryPurchaseNoteStatsRankingSectionItemViewHolder).bind(item)
            }
            is CategoryPurchaseNoteStatsUiItems.PurchaseNoteDateItem -> {
                (holder as PurchaseNoteDateItemViewHolder).bind(item.date)
            }

            is CategoryPurchaseNoteStatsUiItems.PurchaseNoteItem -> {
                (holder as PurchaseNoteItemViewHolder).bind(item.purchaseNote)
            }

            is CategoryPurchaseNoteStatsUiItems.StatsInfoItem -> {
                (holder as CategoryPurchaseNoteStatsInfoItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CategoryPurchaseNoteStatsUiItems.StatsInfoItem -> VIEW_TYPE_STATS_INFO
        is CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem -> VIEW_TYPE_PURCHASE_NAME_RANKING
        is CategoryPurchaseNoteStatsUiItems.PurchaseNoteDateItem -> VIEW_TYPE_PURCHASE_NOTE_DATE
        is CategoryPurchaseNoteStatsUiItems.PurchaseNoteItem -> VIEW_TYPE_PURCHASE_NOTE
        is CategoryPurchaseNoteStatsUiItems.Empty -> VIEW_TYPE_EMPTY
    }

    companion object {
        private const val VIEW_TYPE_STATS_INFO = 1
        private const val VIEW_TYPE_PURCHASE_NAME_RANKING = 2
        private const val VIEW_TYPE_PURCHASE_NOTE_DATE = 3
        private const val VIEW_TYPE_PURCHASE_NOTE = 4
        private const val VIEW_TYPE_EMPTY = 5

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<CategoryPurchaseNoteStatsUiItems>() {
                override fun areItemsTheSame(
                    oldItem: CategoryPurchaseNoteStatsUiItems,
                    newItem: CategoryPurchaseNoteStatsUiItems,
                ): Boolean = when {
                    oldItem is CategoryPurchaseNoteStatsUiItems.StatsInfoItem &&
                            newItem is CategoryPurchaseNoteStatsUiItems.StatsInfoItem -> {
                        true
                    }

                    oldItem is CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem &&
                            newItem is CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem -> {
                        true
                    }

                    oldItem is CategoryPurchaseNoteStatsUiItems.PurchaseNoteDateItem
                            && newItem is CategoryPurchaseNoteStatsUiItems.PurchaseNoteDateItem -> {
                        oldItem.date == newItem.date
                    }

                    oldItem is CategoryPurchaseNoteStatsUiItems.PurchaseNoteItem
                            && newItem is CategoryPurchaseNoteStatsUiItems.PurchaseNoteItem -> {
                        oldItem.purchaseNote.id == newItem.purchaseNote.id
                    }

                    oldItem is CategoryPurchaseNoteStatsUiItems.Empty &&
                            newItem is CategoryPurchaseNoteStatsUiItems.Empty -> {
                        true
                    }

                    else -> false
                }

                override fun areContentsTheSame(
                    oldItem: CategoryPurchaseNoteStatsUiItems,
                    newItem: CategoryPurchaseNoteStatsUiItems,
                ): Boolean = oldItem == newItem
            }
    }
}
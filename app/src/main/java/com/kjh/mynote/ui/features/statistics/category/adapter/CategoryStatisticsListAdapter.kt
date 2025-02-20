package com.kjh.mynote.ui.features.statistics.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.VhCategoryPurchaseNoteStatsRankingSectionItemBinding
import com.kjh.mynote.databinding.VhCategoryPurchaseNotesStatsInfoItemBinding
import com.kjh.mynote.databinding.VhCategoryStatisticsEmptyBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.common.vh.PurchaseNoteDateItemViewHolder
import com.kjh.mynote.ui.common.vh.PurchaseNoteItemViewHolder
import com.kjh.mynote.ui.features.statistics.category.CategoryStatisticsUiItem
import com.kjh.mynote.ui.features.statistics.category.adapter.empty.CategoryStatisticsEmptyItemViewHolder
import com.kjh.mynote.ui.features.statistics.category.adapter.purchasenames.PurchaseNamesSectionItemViewHolder
import com.kjh.mynote.ui.features.statistics.category.adapter.total.TotalStatsSectionItemViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
class CategoryStatisticsListAdapter(
    private val categoryClickAction: () -> Unit,
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
): ListAdapter<CategoryStatisticsUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_STATS_INFO -> {
            TotalStatsSectionItemViewHolder(
                VhCategoryPurchaseNotesStatsInfoItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), categoryClickAction
            )
        }
        VIEW_TYPE_PURCHASE_NAMES -> {
            PurchaseNamesSectionItemViewHolder(
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
        VIEW_TYPE_PURCHASE_NOTE_CONTENTS -> {
            PurchaseNoteItemViewHolder(
                VhPurchaseNoteSearchResultItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }
        VIEW_TYPE_EMPTY -> {
            CategoryStatisticsEmptyItemViewHolder(
                VhCategoryStatisticsEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        else -> throw IllegalStateException("wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryStatisticsUiItem.StatsInfo ->
                (holder as TotalStatsSectionItemViewHolder).bind(item)

            is CategoryStatisticsUiItem.PurchaseNameStats ->
                (holder as PurchaseNamesSectionItemViewHolder).bind(item)

            is CategoryStatisticsUiItem.PurchaseNoteDate ->
                (holder as PurchaseNoteDateItemViewHolder).bind(item.date)

            is CategoryStatisticsUiItem.PurchaseNoteContents ->
                (holder as PurchaseNoteItemViewHolder).bind(item.purchaseNote)

            is CategoryStatisticsUiItem.Empty ->
                (holder as CategoryStatisticsEmptyItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CategoryStatisticsUiItem.StatsInfo -> VIEW_TYPE_STATS_INFO
        is CategoryStatisticsUiItem.PurchaseNameStats -> VIEW_TYPE_PURCHASE_NAMES
        is CategoryStatisticsUiItem.PurchaseNoteDate -> VIEW_TYPE_PURCHASE_NOTE_DATE
        is CategoryStatisticsUiItem.PurchaseNoteContents -> VIEW_TYPE_PURCHASE_NOTE_CONTENTS
        is CategoryStatisticsUiItem.Empty -> VIEW_TYPE_EMPTY
    }

    companion object {
        private const val VIEW_TYPE_STATS_INFO = 1
        private const val VIEW_TYPE_PURCHASE_NAMES = 2
        private const val VIEW_TYPE_PURCHASE_NOTE_DATE = 3
        private const val VIEW_TYPE_PURCHASE_NOTE_CONTENTS = 4
        private const val VIEW_TYPE_EMPTY = 5

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<CategoryStatisticsUiItem>() {
            override fun areItemsTheSame(
                oldItem: CategoryStatisticsUiItem,
                newItem: CategoryStatisticsUiItem,
            ): Boolean = when {
                oldItem is CategoryStatisticsUiItem.StatsInfo &&
                        newItem is CategoryStatisticsUiItem.StatsInfo -> {
                            true
                        }

                oldItem is CategoryStatisticsUiItem.PurchaseNameStats &&
                        newItem is CategoryStatisticsUiItem.PurchaseNameStats -> {
                            true
                        }

                oldItem is CategoryStatisticsUiItem.PurchaseNoteDate &&
                        newItem is CategoryStatisticsUiItem.PurchaseNoteDate -> {
                            oldItem.date == newItem.date
                        }

                oldItem is CategoryStatisticsUiItem.PurchaseNoteContents &&
                        newItem is CategoryStatisticsUiItem.PurchaseNoteContents -> {
                            oldItem.purchaseNote.id == newItem.purchaseNote.id
                        }

                oldItem is CategoryStatisticsUiItem.Empty &&
                        newItem is CategoryStatisticsUiItem.Empty -> {
                            true
                        }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: CategoryStatisticsUiItem,
                newItem: CategoryStatisticsUiItem,
            ): Boolean = oldItem == newItem
        }
    }
}
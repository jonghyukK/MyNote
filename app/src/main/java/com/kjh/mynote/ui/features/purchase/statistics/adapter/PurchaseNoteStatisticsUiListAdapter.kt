package com.kjh.mynote.ui.features.purchase.statistics.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.LayoutEmptyMyPurchasesBinding
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.ui.features.category.purchasestats.adapter.PurchaseNotesEmptyItemViewHolder
import com.kjh.mynote.ui.features.purchase.statistics.PurchaseNoteStaticsUiItem
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */
class PurchaseNoteStatisticsUiListAdapter(
    private val dateClickAction: () -> Unit,
    private val sliceClickAction: (PieEntry?) -> Unit,
): ListAdapter<PurchaseNoteStaticsUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_STATS_INFO -> {
                PurchaseNoteStatisticsInfoItemViewHolder(
                    VhPurchaseNoteStatsInfoItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), dateClickAction
                )
            }
            VIEW_TYPE_PIE_CHART -> {
                PieChartItemViewHolder(
                    VhPurchaseNoteStatsPieChartItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), sliceClickAction
                )
            }
            VIEW_TYPE_CATEGORY_STATS -> {
                CategoryStatsItemViewHolder(
                    VhHomeCategoryPutchaseStatsItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            VIEW_TYPE_EMPTY -> {
                PurchaseNotesEmptyItemViewHolder(
                    LayoutEmptyMyPurchasesBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), {}
                )
            }
            else -> throw IllegalArgumentException("Wrong ViewType: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PurchaseNoteStaticsUiItem.StatsInfoItem -> {
                (holder as PurchaseNoteStatisticsInfoItemViewHolder).bind(item)
            }
            is PurchaseNoteStaticsUiItem.PieChartItem -> {
                (holder as PieChartItemViewHolder).bind(item)
            }
            is PurchaseNoteStaticsUiItem.CategoryStatsItem -> {
                (holder as CategoryStatsItemViewHolder).bind(item)
            }
            is PurchaseNoteStaticsUiItem.Empty -> {
                (holder as PurchaseNotesEmptyItemViewHolder).bind(Unit)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNoteStaticsUiItem.StatsInfoItem -> VIEW_TYPE_STATS_INFO
        is PurchaseNoteStaticsUiItem.PieChartItem -> VIEW_TYPE_PIE_CHART
        is PurchaseNoteStaticsUiItem.CategoryStatsItem -> VIEW_TYPE_CATEGORY_STATS
        is PurchaseNoteStaticsUiItem.Empty -> VIEW_TYPE_EMPTY
    }

    companion object {
        private const val VIEW_TYPE_STATS_INFO = 1
        private const val VIEW_TYPE_PIE_CHART = 2
        private const val VIEW_TYPE_CATEGORY_STATS = 3
        private const val VIEW_TYPE_EMPTY = 4

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNoteStaticsUiItem>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNoteStaticsUiItem,
                    newItem: PurchaseNoteStaticsUiItem,
                ): Boolean = when {
                    oldItem is PurchaseNoteStaticsUiItem.StatsInfoItem &&
                            newItem is PurchaseNoteStaticsUiItem.StatsInfoItem -> {
                                true
                            }
                    oldItem is PurchaseNoteStaticsUiItem.PieChartItem &&
                            newItem is PurchaseNoteStaticsUiItem.PieChartItem -> {
                                oldItem.pieEntries == newItem.pieEntries
                            }
                    oldItem is PurchaseNoteStaticsUiItem.CategoryStatsItem &&
                            newItem is PurchaseNoteStaticsUiItem.CategoryStatsItem -> {
                                oldItem.categoryStatsItem.categoryId == newItem.categoryStatsItem.categoryId
                            }
                    oldItem is PurchaseNoteStaticsUiItem.Empty &&
                            newItem is PurchaseNoteStaticsUiItem.Empty -> {
                                true
                            }
                    else -> false
                }

                override fun areContentsTheSame(
                    oldItem: PurchaseNoteStaticsUiItem,
                    newItem: PurchaseNoteStaticsUiItem,
                ): Boolean = oldItem == newItem
            }
    }
}


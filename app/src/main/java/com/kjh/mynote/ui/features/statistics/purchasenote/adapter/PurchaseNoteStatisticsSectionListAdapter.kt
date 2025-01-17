package com.kjh.mynote.ui.features.purchase.statistics.adapter.section

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryStats
import com.example.domain.model.PaymentMethodStats
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

class PurchaseNoteStatisticsSectionListAdapter(
    private val dateClickAction: () -> Unit,
    private val categoryPieSliceClickAction: (PieEntry?) -> Unit,
    private val paymentMethodPieSliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsClickAction: (CategoryStats) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStats) -> Unit,
    private val categoryStatsMoreClickAction: () -> Unit,
    private val paymentMethodStatsMoreClickAction: () -> Unit,
): ListAdapter<PurchaseNoteStatisticsUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_STATS_TOTAL -> {
                TotalStatsSectionItemViewHolder(
                    VhPurchaseNoteStatsInfoItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), dateClickAction
                )
            }
            VIEW_TYPE_STATS_CATEGORY_SECTION -> {
                CategoryStatsSectionItemViewHolder(
                    VhPurchaseNoteStatsPieChartItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ),
                    categoryPieSliceClickAction,
                    categoryStatsClickAction,
                    paymentMethodStatsClickAction,
                    categoryStatsMoreClickAction
                )
            }
            VIEW_TYPE_STATS_PAYMENT_METHOD_SECTION -> {
                PaymentMethodStatsSectionItemViewHolder(
                    VhPurchaseNoteStatsPieChartItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ),
                    paymentMethodPieSliceClickAction,
                    categoryStatsClickAction,
                    paymentMethodStatsClickAction,
                    paymentMethodStatsMoreClickAction
                )
            }
            else -> throw IllegalArgumentException("Wrong ViewType: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PurchaseNoteStatisticsUiItem.StatsTotalSection -> {
                (holder as TotalStatsSectionItemViewHolder).bind(item)
            }
            is PurchaseNoteStatisticsUiItem.CategoryStatsSection -> {
                (holder as CategoryStatsSectionItemViewHolder).bind(item)
            }
            is PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection -> {
                (holder as PaymentMethodStatsSectionItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNoteStatisticsUiItem.StatsTotalSection -> VIEW_TYPE_STATS_TOTAL
        is PurchaseNoteStatisticsUiItem.CategoryStatsSection -> VIEW_TYPE_STATS_CATEGORY_SECTION
        is PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection -> VIEW_TYPE_STATS_PAYMENT_METHOD_SECTION
    }

    companion object {
        private const val VIEW_TYPE_STATS_TOTAL = 1
        private const val VIEW_TYPE_STATS_CATEGORY_SECTION = 2
        private const val VIEW_TYPE_STATS_PAYMENT_METHOD_SECTION = 3

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNoteStatisticsUiItem>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNoteStatisticsUiItem,
                    newItem: PurchaseNoteStatisticsUiItem,
                ): Boolean = when {
                    oldItem is PurchaseNoteStatisticsUiItem.StatsTotalSection &&
                            newItem is PurchaseNoteStatisticsUiItem.StatsTotalSection -> {
                                true
                            }
                    oldItem is PurchaseNoteStatisticsUiItem.CategoryStatsSection &&
                            newItem is PurchaseNoteStatisticsUiItem.CategoryStatsSection -> {
                                oldItem.pieChartItem.pieEntries == newItem.pieChartItem.pieEntries
                            }
                    oldItem is PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection &&
                            newItem is PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection -> {
                                oldItem.pieChartItem.pieEntries == newItem.pieChartItem.pieEntries
                            }
                    else -> false
                }

                override fun areContentsTheSame(
                    oldItem: PurchaseNoteStatisticsUiItem,
                    newItem: PurchaseNoteStatisticsUiItem,
                ): Boolean = oldItem == newItem
            }
    }
}


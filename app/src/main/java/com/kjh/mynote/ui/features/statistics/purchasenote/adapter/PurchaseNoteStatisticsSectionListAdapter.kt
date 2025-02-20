package com.kjh.mynote.ui.features.statistics.purchasenote.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.ui.features.statistics.purchasenote.CategoryStatsItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PaymentMethodStatsItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PieChartItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.category.CategoryStatsSectionItemViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.paymentmethod.PaymentMethodStatsSectionItemViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.adapter.total.TotalStatsSectionItemViewHolder
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.parcelableArrayList

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

class PurchaseNoteStatisticsSectionListAdapter(
    private val dateClickAction: () -> Unit,
    private val categoryPieSliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsClickAction: (CategoryStatsUiModel) -> Unit,
    private val categoryStatsMoreClickAction: () -> Unit,
    private val paymentMethodPieSliceClickAction: (PieEntry?) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStatsUiModel) -> Unit,
    private val paymentMethodStatsMoreClickAction: () -> Unit,
): ListAdapter<PurchaseNoteStatisticsUiItemState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

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
                    categoryStatsMoreClickAction
                )
            }
            VIEW_TYPE_STATS_PAYMENT_METHOD_SECTION -> {
                PaymentMethodStatsSectionItemViewHolder(
                    VhPurchaseNoteStatsPieChartItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ),
                    paymentMethodPieSliceClickAction,
                    paymentMethodStatsClickAction,
                    paymentMethodStatsMoreClickAction
                )
            }
            else -> throw IllegalArgumentException("Wrong ViewType: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PurchaseNoteStatisticsUiItemState.StatsTotalSection -> {
                (holder as TotalStatsSectionItemViewHolder).bind(item)
            }
            is PurchaseNoteStatisticsUiItemState.CategoryStatsSection -> {
                (holder as CategoryStatsSectionItemViewHolder).bind(item)
            }
            is PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection -> {
                (holder as PaymentMethodStatsSectionItemViewHolder).bind(item)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty()) {
            val bundle = payloads[0] as Bundle

            if (bundle.containsKey(BUNDLE_KEY_CATEGORY_PIE_CHART)) {
                bundle.parcelable<PieChartItem>(BUNDLE_KEY_CATEGORY_PIE_CHART)?.let {
                    (holder as CategoryStatsSectionItemViewHolder).updatePieChartData(it)
                }
            }

            if (bundle.containsKey(BUNDLE_KEY_CATEGORY_PIE_HIGH_LIGHTS)) {
                bundle.parcelable<PieChartItem>(BUNDLE_KEY_CATEGORY_PIE_HIGH_LIGHTS)?.let {
                    (holder as CategoryStatsSectionItemViewHolder).updateHighlightedEntry(it)
                }
            }

            if (bundle.containsKey(BUNDLE_KEY_CATEGORY_EXPANDED) ||
                bundle.containsKey(BUNDLE_KEY_CATEGORY_STATS_ITEMS)
            ) {
                (holder as CategoryStatsSectionItemViewHolder).updateCategoryStatsList(
                    isExpanded = bundle.getBoolean(BUNDLE_KEY_CATEGORY_EXPANDED),
                    categoryStatsList = bundle.parcelableArrayList<CategoryStatsItem>(
                        BUNDLE_KEY_CATEGORY_STATS_ITEMS
                    )?.toList() ?: emptyList()
                )
            }

            if (bundle.containsKey(BUNDLE_KEY_PAYMENT_PIE_CHART)) {
                bundle.parcelable<PieChartItem>(BUNDLE_KEY_PAYMENT_PIE_CHART)?.let {
                    (holder as PaymentMethodStatsSectionItemViewHolder).updatePieChartData(it)
                }
            }

            if (bundle.containsKey(BUNDLE_KEY_PAYMENT_PIE_HIGH_LIGHTS)) {
                bundle.parcelable<PieChartItem>(BUNDLE_KEY_PAYMENT_PIE_HIGH_LIGHTS)?.let {
                    (holder as PaymentMethodStatsSectionItemViewHolder).updateHighlightedEntry(it)
                }
            }

            if (bundle.containsKey(BUNDLE_KEY_PAYMENT_EXPANDED) ||
                bundle.containsKey(BUNDLE_KEY_PAYMENT_STATS_ITEMS)
            ) {
                (holder as PaymentMethodStatsSectionItemViewHolder).updatePaymentMethodStatsList(
                    isExpanded = bundle.getBoolean(BUNDLE_KEY_PAYMENT_EXPANDED),
                    statsList = bundle.parcelableArrayList<PaymentMethodStatsItem>(
                        BUNDLE_KEY_PAYMENT_STATS_ITEMS
                    )?.toList() ?: emptyList()
                )
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PurchaseNoteStatisticsUiItemState.StatsTotalSection -> VIEW_TYPE_STATS_TOTAL
        is PurchaseNoteStatisticsUiItemState.CategoryStatsSection -> VIEW_TYPE_STATS_CATEGORY_SECTION
        is PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection -> VIEW_TYPE_STATS_PAYMENT_METHOD_SECTION
    }

    companion object {
        private const val VIEW_TYPE_STATS_TOTAL = 1
        private const val VIEW_TYPE_STATS_CATEGORY_SECTION = 2
        private const val VIEW_TYPE_STATS_PAYMENT_METHOD_SECTION = 3

        private const val BUNDLE_KEY_CATEGORY_PIE_CHART = "categoryPieChart"
        private const val BUNDLE_KEY_CATEGORY_STATS_ITEMS = "categoryStatsItems"
        private const val BUNDLE_KEY_CATEGORY_PIE_HIGH_LIGHTS = "categoryHighlight"
        private const val BUNDLE_KEY_CATEGORY_EXPANDED = "categoryStatsExpanded"

        private const val BUNDLE_KEY_PAYMENT_PIE_CHART = "paymentPieChart"
        private const val BUNDLE_KEY_PAYMENT_STATS_ITEMS = "paymentStatsItems"
        private const val BUNDLE_KEY_PAYMENT_PIE_HIGH_LIGHTS = "paymentHighlight"
        private const val BUNDLE_KEY_PAYMENT_EXPANDED = "paymentStatsExpanded"

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PurchaseNoteStatisticsUiItemState>() {
            override fun areItemsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState,
                newItem: PurchaseNoteStatisticsUiItemState,
            ): Boolean = oldItem::class == newItem::class

            override fun areContentsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState,
                newItem: PurchaseNoteStatisticsUiItemState,
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: PurchaseNoteStatisticsUiItemState,
                newItem: PurchaseNoteStatisticsUiItemState,
            ): Any? {
                val diffBundle = Bundle()

                when {
                    oldItem is PurchaseNoteStatisticsUiItemState.CategoryStatsSection &&
                            newItem is PurchaseNoteStatisticsUiItemState.CategoryStatsSection -> {

                        if (oldItem.pieChartItem != newItem.pieChartItem) {
                            when {
                                oldItem.pieChartItem.pieEntries != newItem.pieChartItem.pieEntries ->
                                    diffBundle.putParcelable(BUNDLE_KEY_CATEGORY_PIE_CHART, newItem.pieChartItem)

                                oldItem.pieChartItem.highlightedPieEntry != newItem.pieChartItem.highlightedPieEntry ->
                                    diffBundle.putParcelable(BUNDLE_KEY_CATEGORY_PIE_HIGH_LIGHTS, newItem.pieChartItem)
                            }
                        }

                        if (oldItem.isExpanded != newItem.isExpanded || oldItem.childItems != newItem.childItems) {
                            diffBundle.putBoolean(BUNDLE_KEY_CATEGORY_EXPANDED, newItem.isExpanded)
                            diffBundle.putParcelableArrayList(BUNDLE_KEY_CATEGORY_STATS_ITEMS, ArrayList(newItem.childItems))
                        }
                    }

                    oldItem is PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection &&
                            newItem is PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection -> {
                        if (oldItem.pieChartItem != newItem.pieChartItem) {
                            when {
                                oldItem.pieChartItem.pieEntries != newItem.pieChartItem.pieEntries ->
                                    diffBundle.putParcelable(BUNDLE_KEY_PAYMENT_PIE_CHART, newItem.pieChartItem)

                                oldItem.pieChartItem.highlightedPieEntry != newItem.pieChartItem.highlightedPieEntry ->
                                    diffBundle.putParcelable(BUNDLE_KEY_PAYMENT_PIE_HIGH_LIGHTS, newItem.pieChartItem)
                            }
                        }

                        if (oldItem.isExpanded != newItem.isExpanded || oldItem.childItems != newItem.childItems) {
                            diffBundle.putBoolean(BUNDLE_KEY_PAYMENT_EXPANDED, newItem.isExpanded)
                            diffBundle.putParcelableArrayList(BUNDLE_KEY_PAYMENT_STATS_ITEMS, ArrayList(newItem.childItems))
                        }
                    }
                }

                return if (diffBundle.size() == 0) null else diffBundle
            }
        }
    }
}

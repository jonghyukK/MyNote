package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.paymentmethod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.ui.features.statistics.purchasenote.PaymentMethodStatsItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PieChartItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.parcelableArrayList

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */
class PaymentMethodStatsSectionAdapter(
    private val paymentMethodPieSliceClickAction: (PieEntry?) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStatsUiModel) -> Unit,
    private val paymentMethodStatsMoreClickAction: () -> Unit,
) : ListAdapter<PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection, PaymentMethodStatsSectionItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int,
    ) = PaymentMethodStatsSectionItemViewHolder(
        VhPurchaseNoteStatsPieChartItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
        paymentMethodPieSliceClickAction,
        paymentMethodStatsClickAction,
        paymentMethodStatsMoreClickAction
    )

    override fun onBindViewHolder(holder: PaymentMethodStatsSectionItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: PaymentMethodStatsSectionItemViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty()) {
            val bundle = payloads[0] as Bundle

            if (bundle.containsKey(BUNDLE_KEY_PIE_CHART)) {
                bundle.parcelable<PieChartItem>(BUNDLE_KEY_PIE_CHART)?.let {
                    holder.updatePieChartData(it)
                }
            }

            if (bundle.containsKey(BUNDLE_KEY_PIE_HIGH_LIGHTS)) {
                bundle.parcelable<PieChartItem>(BUNDLE_KEY_PIE_HIGH_LIGHTS)?.let {
                    holder.updateHighlightedEntry(it)
                }
            }

            if (bundle.containsKey(BUNDLE_KEY_EXPANDED) ||
                bundle.containsKey(BUNDLE_KEY_STATS_ITEMS)
            ) {
                holder.updatePaymentMethodStatsList(
                    isExpanded = bundle.getBoolean(BUNDLE_KEY_EXPANDED),
                    statsList = bundle.parcelableArrayList<PaymentMethodStatsItem>(
                        BUNDLE_KEY_STATS_ITEMS
                    )?.toList() ?: emptyList()
                )
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object {
        private const val BUNDLE_KEY_PIE_CHART = "pieChart"
        private const val BUNDLE_KEY_STATS_ITEMS = "statsItems"
        private const val BUNDLE_KEY_PIE_HIGH_LIGHTS = "highlight"
        private const val BUNDLE_KEY_EXPANDED = "statsExpanded"

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection>() {
            override fun areItemsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection,
                newItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection,
            ): Boolean = oldItem::class == newItem::class

            override fun areContentsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection,
                newItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection,
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection,
                newItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection,
            ): Any? {
                val diffBundle = Bundle()

                if (oldItem.pieChartItem != newItem.pieChartItem) {
                    when {
                        oldItem.pieChartItem.pieEntries != newItem.pieChartItem.pieEntries ->
                            diffBundle.putParcelable(BUNDLE_KEY_PIE_CHART, newItem.pieChartItem)

                        oldItem.pieChartItem.highlightedPieEntry != newItem.pieChartItem.highlightedPieEntry ->
                            diffBundle.putParcelable(BUNDLE_KEY_PIE_HIGH_LIGHTS, newItem.pieChartItem)
                    }
                }

                if (oldItem.isExpanded != newItem.isExpanded || oldItem.childItems != newItem.childItems) {
                    diffBundle.putBoolean(BUNDLE_KEY_EXPANDED, newItem.isExpanded)
                    diffBundle.putParcelableArrayList(BUNDLE_KEY_STATS_ITEMS, ArrayList(newItem.childItems))
                }

                return if (diffBundle.size() == 0) null else diffBundle
            }
        }
    }
}
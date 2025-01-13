package com.kjh.mynote.ui.features.purchase.statistics.adapter.section

import androidx.core.content.ContextCompat
import com.example.domain.model.CategoryStats
import com.example.domain.model.PaymentMethodStats
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.purchase.statistics.PieChartItem
import com.kjh.mynote.ui.features.purchase.statistics.PurchaseNoteStatisticsUiItem
import com.kjh.mynote.ui.features.purchase.statistics.SeeAllEvent
import com.kjh.mynote.ui.features.purchase.statistics.adapter.contents.StatsChildListAdapter

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

/**
 * 구매노트 통계 화면 - 결제수단별 통계 ViewHolder.
 *
 * @property binding
 * @property paymentMethodPieSliceClickAction
 * @property categoryStatsClickAction
 * @property paymentMethodStatsClickAction
 * @property showAllClickAction
 */
class PaymentMethodStatsSectionItemViewHolder(
    private val binding: VhPurchaseNoteStatsPieChartItemBinding,
    private val paymentMethodPieSliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsClickAction: (CategoryStats) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStats) -> Unit,
    private val showAllClickAction: (SeeAllEvent) -> Unit,
): BaseViewHolder<PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection>(binding.root) {

    private val childListAdapter = StatsChildListAdapter(
        categoryStatsClickAction, paymentMethodStatsClickAction, showAllClickAction
    )

    init {
        binding.rvChildList.apply {
            itemAnimator = null
            adapter = childListAdapter
        }

        binding.chart.setChartValueSelectedListener(object: OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                bindItem?.let { paymentMethodPieSliceClickAction(e as PieEntry) }
            }

            override fun onNothingSelected() {
                bindItem?.let { paymentMethodPieSliceClickAction(null) }
            }
        })
    }

    override fun bind(item: PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection) {
        super.bind(item)

        setupPieChartUi(item.pieChartItem)

        childListAdapter.submitList(item.childItems)
    }

    private fun setupPieChartUi(pieChartItem: PieChartItem) = with (binding) {
        val centerText = pieChartItem.highlightedPieEntry?.let {
            "구매노트 ${it.value.toInt()}건"
        } ?: ""

        val pieColors = pieChartItem.pieColors.map { ContextCompat.getColor(context, it) }

        tvSectionTitle.text = context.getString(R.string.title_statistics_for_each_payment_method)

        chart.setCenterText(centerText)
        chart.setupPieData(pieChartItem.pieEntries, pieColors, pieChartItem.isEmpty)
        chart.updateHighlight(pieChartItem.highlightedPieEntry)
    }
}
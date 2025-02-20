package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.paymentmethod
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.PaymentMethodStatsItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PieChartItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.extensions.onThrottleClick

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
 * @property paymentMethodStatsClickAction
 * @property paymentMethodStatsMoreClickAction
 */
class PaymentMethodStatsSectionItemViewHolder(
    private val binding: VhPurchaseNoteStatsPieChartItemBinding,
    private val paymentMethodPieSliceClickAction: (PieEntry?) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStatsUiModel) -> Unit,
    private val paymentMethodStatsMoreClickAction: () -> Unit,
): BaseViewHolder<PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection>(binding.root) {

    private val paymentMethodStatsListAdapter = PaymentMethodStatsListAdapter(paymentMethodStatsClickAction)

    init {
        binding.rvChildList.apply {
            adapter = paymentMethodStatsListAdapter
        }

        binding.tvMore.onThrottleClick {
            bindItem?.let { paymentMethodStatsMoreClickAction() }
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

    override fun bind(item: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection) {
        super.bind(item)

        binding.tvSectionTitle.text = context.getString(R.string.title_statistics_for_each_payment_method)

        bindPieChartData(item.pieChartItem)
        bindHighlightedEntryWithCenterText(item.pieChartItem.highlightedPieEntry)
        bindMoreBtnVisibility(item.isVisibleMoreBtn)
        bindPaymentMethodStatsList(item.isExpanded, item.childItems)
    }

    private fun bindPaymentMethodStatsList(
        isExpanded: Boolean,
        paymentMethodStatsList: List<PaymentMethodStatsItem>
    ) {
        val items = if (isExpanded) paymentMethodStatsList else paymentMethodStatsList.take(5)
        paymentMethodStatsListAdapter.submitList(items)
    }

    private fun bindMoreBtnVisibility(isVisible: Boolean) = with (binding) {
        tvMore.isVisible = isVisible
    }

    private fun bindPieChartData(pieChartItem: PieChartItem) = with (binding) {
        val pieColors = pieChartItem.pieColors.map { ContextCompat.getColor(context, it) }
        chart.setupPieData(pieChartItem.pieEntries, pieColors, pieChartItem.isEmpty)
    }

    private fun bindHighlightedEntryWithCenterText(highlightedEntry: PieEntry?) = with (binding) {
        val centerText = highlightedEntry?.data?.toString() ?: ""
        chart.setCenterText(centerText)
        chart.updateHighlight(highlightedEntry)
    }

    fun updatePieChartData(pieChartItem: PieChartItem) {
        bindItem?.let {
            super.bind(it.copy(pieChartItem = pieChartItem))
        }
        bindPieChartData(pieChartItem)
        bindHighlightedEntryWithCenterText(pieChartItem.highlightedPieEntry)
    }

    fun updateHighlightedEntry(pieChartItem: PieChartItem) {
        bindItem?.let {
            super.bind(it.copy(pieChartItem = pieChartItem))
        }
        bindHighlightedEntryWithCenterText(pieChartItem.highlightedPieEntry)
    }

    fun updatePaymentMethodStatsList(isExpanded: Boolean, statsList: List<PaymentMethodStatsItem>) {
        bindItem?.let {
            super.bind(it.copy(isExpanded = isExpanded, childItems = statsList))
        }

        bindMoreBtnVisibility(!isExpanded && statsList.size > 5)
        bindPaymentMethodStatsList(isExpanded, statsList)
    }
}
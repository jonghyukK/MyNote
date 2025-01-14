package com.kjh.mynote.ui.features.purchase.statistics.adapter.section

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import com.kjh.mynote.ui.features.purchase.statistics.adapter.section.contents.StatsChildListAdapter
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

/**
 * 구매노트 통계 화면 - 카테고리별 통계 ViewHolder.
 *
 * @property binding
 * @property categoryPieSliceClickAction
 * @property categoryStatsClickAction
 * @property paymentMethodStatsClickAction
 * @property categoryStatsMoreClickAction
 */
class CategoryStatsSectionItemViewHolder(
    private val binding: VhPurchaseNoteStatsPieChartItemBinding,
    private val categoryPieSliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsClickAction: (CategoryStats) -> Unit,
    private val paymentMethodStatsClickAction: (PaymentMethodStats) -> Unit,
    private val categoryStatsMoreClickAction: () -> Unit
): BaseViewHolder<PurchaseNoteStatisticsUiItem.CategoryStatsSection>(binding.root) {

    private val childListAdapter = StatsChildListAdapter(
        categoryStatsClickAction, paymentMethodStatsClickAction)

    init {
        binding.rvChildList.apply {
            itemAnimator = null
            adapter = childListAdapter
        }

        binding.tvMore.onThrottleClick {
            bindItem?.let { categoryStatsMoreClickAction() }
        }

        binding.chart.setChartValueSelectedListener(object: OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                bindItem?.let { categoryPieSliceClickAction(e as PieEntry) }
            }

            override fun onNothingSelected() {
                bindItem?.let { categoryPieSliceClickAction(null) }
            }
        })
    }

    override fun bind(item: PurchaseNoteStatisticsUiItem.CategoryStatsSection) {
        super.bind(item)

        setupPieChartUi(item.pieChartItem)

        binding.tvMore.isVisible = !item.isExpanded && item.childItems.size > 5

        val childItems = if (item.isExpanded) item.childItems else item.childItems.take(5)
        childListAdapter.submitList(childItems)
    }

    private fun setupPieChartUi(pieChartItem: PieChartItem) = with (binding) {
        val centerText = pieChartItem.highlightedPieEntry?.data?.toString() ?: ""
        val pieColors = pieChartItem.pieColors.map { ContextCompat.getColor(context, it) }

        tvSectionTitle.text = context.getString(R.string.title_statistics_for_each_category)

        chart.setCenterText(centerText)
        chart.setupPieData(pieChartItem.pieEntries, pieColors, pieChartItem.isEmpty)
        chart.updateHighlight(pieChartItem.highlightedPieEntry)
    }
}
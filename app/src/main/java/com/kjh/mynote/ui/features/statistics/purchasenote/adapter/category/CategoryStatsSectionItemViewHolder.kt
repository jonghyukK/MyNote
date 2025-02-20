package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.category

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.CategoryStatsItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PieChartItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.extensions.onThrottleClick
import timber.log.Timber

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
 * @property categoryStatsMoreClickAction
 */
class CategoryStatsSectionItemViewHolder(
    private val binding: VhPurchaseNoteStatsPieChartItemBinding,
    private val categoryPieSliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsClickAction: (CategoryStatsUiModel) -> Unit,
    private val categoryStatsMoreClickAction: () -> Unit
): BaseViewHolder<PurchaseNoteStatisticsUiItemState.CategoryStatsSection>(binding.root) {

    private val categoryStatsListAdapter = CategoryStatsListAdapter(categoryStatsClickAction)

    init {
        binding.rvChildList.apply {
            itemAnimator = null
            adapter = categoryStatsListAdapter
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

    override fun bind(item: PurchaseNoteStatisticsUiItemState.CategoryStatsSection) {
        super.bind(item)

        binding.tvSectionTitle.text = context.getString(R.string.title_statistics_for_each_category)

        bindPieChartData(item.pieChartItem)
        bindHighlightedEntryWithCenterText(item.pieChartItem.highlightedPieEntry)
        bindMoreBtnVisibility(item.isVisibleMoreBtn)
        bindCategoryStatsList(item.isExpanded, item.childItems)
    }

    private fun bindCategoryStatsList(isExpanded: Boolean, categoryStatsList: List<CategoryStatsItem>) {
        val items = if (isExpanded) categoryStatsList else categoryStatsList.take(5)
        categoryStatsListAdapter.submitList(items)
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

    fun updateCategoryStatsList(isExpanded: Boolean, categoryStatsList: List<CategoryStatsItem>) {
        bindItem?.let {
            super.bind(it.copy(isExpanded = isExpanded, childItems = categoryStatsList))
        }

        bindMoreBtnVisibility(!isExpanded && categoryStatsList.size > 5)
        bindCategoryStatsList(isExpanded, categoryStatsList)
    }
}
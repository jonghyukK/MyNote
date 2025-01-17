package com.kjh.mynote.ui.features.home.adapter.statistics

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.domain.model.CategoryStats
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutHomePieChartLegendBinding
import com.kjh.mynote.databinding.VhHomePieChartItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.home.HomeUiItem
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 10..
 * Description:
 */

class HomePurchaseNoteStatisticsSectionItemViewHolder(
    private val binding: VhHomePieChartItemBinding,
    private val sliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsItemClickAction: (CategoryStats) -> Unit,
    private val seeAllPurchaseStatsClickAction: () -> Unit
): BaseViewHolder<HomeUiItem.HomeMonthlyPurchaseStatisticsItem>(binding.root) {

    private val categoryPurchaseStatsListAdapter = HomeCategoryStatsListAdapter(
        categoryStatsItemClickAction = categoryStatsItemClickAction,
        seeAllPurchaseStatsClickAction = seeAllPurchaseStatsClickAction
    )

    private var legendViews: List<View> = emptyList()

    init {
        binding.clShowAll.onThrottleClick {
            bindItem?.let { seeAllPurchaseStatsClickAction() }
        }

        binding.chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isRotationEnabled = false
            holeRadius = 55f
            transparentCircleRadius = 56f
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setTransparentCircleAlpha(20)
            setTransparentCircleColor(R.color.black_900)
            setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    sliceClickAction(e as PieEntry)
                }
                override fun onNothingSelected() {
                    sliceClickAction(null)
                    centerText = null
                }
            })

            animateY(1000, Easing.EaseInOutCubic)
            animate()
        }

        binding.rvCategories.apply {
            itemAnimator = null
            adapter = categoryPurchaseStatsListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(top = 6))
            }
        }
    }

    override fun bind(item: HomeUiItem.HomeMonthlyPurchaseStatisticsItem) {
        super.bind(item)

        setupPieChart(item)
        setupLegendViews(item)
        updateLegendHighlight(item.highlightedPieEntry)

        binding.tvTotalNoteCount.text = "전체 ${item.totalNoteCount}건"
        binding.tvTotalNotePrice.text = "총 ${item.totalNotePrice.toComma()}원"
        binding.rvCategories.isVisible = item.categoryStatsUiItems.isNotEmpty()

        categoryPurchaseStatsListAdapter.submitList(item.categoryStatsUiItems)
    }

    private fun setupPieChart(item: HomeUiItem.HomeMonthlyPurchaseStatisticsItem) {
        val pieDataSet = PieDataSet(item.pieEntries, "").apply {
            colors = item.pieColors.map { ContextCompat.getColor(context, it) }
            sliceSpace = 3f
            setDrawValues(false)
        }

        with (binding) {
            chart.centerText = item.highlightedPieEntry?.let { "구매노트 ${it.value.toInt()}건" }
            chart.isHighlightPerTapEnabled = item.categoryStatsUiItems.isNotEmpty()
            chart.data = PieData(pieDataSet)
        }
    }

    private fun setupLegendViews(item: HomeUiItem.HomeMonthlyPurchaseStatisticsItem) {
        binding.llLegendContainer.removeAllViews()
        legendViews = makeLegendViews(item)
        legendViews.forEach { binding.llLegendContainer.addView(it) }
    }

    private fun makeLegendViews(chartItem: HomeUiItem.HomeMonthlyPurchaseStatisticsItem): List<View> {
        return chartItem.pieEntries.indices.map { i ->
            val legendBinding = LayoutHomePieChartLegendBinding.inflate(
                LayoutInflater.from(context), binding.llLegendContainer, false)

            val categoryName = chartItem.pieEntries[i].label

            with (legendBinding) {
                tvLegendName.text = categoryName
                viewLegendColor.setBackgroundColor(
                    ContextCompat.getColor(context, chartItem.pieColors[i]))

                if (chartItem.categoryStatsUiItems.isNotEmpty()) {
                    root.tag = categoryName
                    root.addClickAnimation()
                    root.onThrottleClick {
                        sliceClickAction(chartItem.pieEntries[i])
                    }
                }
            }

            legendBinding.root
        }
    }

    private fun updateLegendHighlight(highlightedPieEntry: PieEntry?) {
        legendViews.forEach { view ->
            val legendBinding = LayoutHomePieChartLegendBinding.bind(view)

            val isHighlighted = view.tag == (highlightedPieEntry?.label ?: "")
            val typeFace = if (isHighlighted) Typeface.BOLD else Typeface.NORMAL

            legendBinding.tvLegendName.setTypeface(null, typeFace)
        }

        val entryIndex = binding.chart.data.dataSet.getEntryIndex(highlightedPieEntry)
        if (entryIndex >= 0) {
            binding.chart.highlightValue(entryIndex.toFloat(), 0f, 0)
        } else {
            binding.chart.highlightValues(arrayOf())
        }
    }
}
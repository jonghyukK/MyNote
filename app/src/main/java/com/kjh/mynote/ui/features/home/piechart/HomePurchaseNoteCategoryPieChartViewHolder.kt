package com.kjh.mynote.ui.features.home.piechart

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
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
import com.kjh.mynote.ui.features.home.HomeItem
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 10..
 * Description:
 */

class HomePurchaseNoteCategoryPieChartViewHolder(
    private val binding: VhHomePieChartItemBinding,
    private val sliceClickAction: (PieEntry?) -> Unit
): BaseViewHolder<HomeItem.HomePurchaseNoteCategoryPirChart>(binding.root) {

    private var legendViews: List<View> = emptyList()

    init {
        binding.chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            isRotationEnabled = false
            holeRadius = 55f
            transparentCircleRadius = 56f
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setTouchEnabled(true)
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

            animateY(1200, Easing.EaseInOutCubic)
            animate()
        }
    }

    override fun bind(item: HomeItem.HomePurchaseNoteCategoryPirChart) {
        super.bind(item)

        setupPieChart(item)
        setupLegendViews(item)
        updateLegendHighlight(item.highlightedPieEntry)
    }

    private fun setupPieChart(item: HomeItem.HomePurchaseNoteCategoryPirChart) {
        val pieDataSet = PieDataSet(item.pieEntries, "").apply {
            colors = item.pieColors.map { ContextCompat.getColor(context, it) }
            sliceSpace = 3f
            setDrawValues(false)
        }

        with (binding) {
            chart.centerText = item.highlightedPieEntry?.let { "구매노트 ${it.value.toInt()}건" }
            chart.data = PieData(pieDataSet)
            chart.isHighlightPerTapEnabled = item.categoryWithCountItems.isNotEmpty()
        }
    }

    private fun setupLegendViews(item: HomeItem.HomePurchaseNoteCategoryPirChart) {
        binding.llLegendContainer.removeAllViews()
        legendViews = makeLegendViews(item)
        legendViews.forEach { binding.llLegendContainer.addView(it) }
    }

    private fun makeLegendViews(chartItem: HomeItem.HomePurchaseNoteCategoryPirChart): List<View> {
        return chartItem.pieEntries.indices.map { i ->
            val legendBinding = LayoutHomePieChartLegendBinding.inflate(
                LayoutInflater.from(context), binding.llLegendContainer, false)

            val categoryName = chartItem.pieEntries[i].label

            with (legendBinding) {
                tvCategoryName.text = categoryName
                viewLegendColor.setBackgroundColor(
                    ContextCompat.getColor(context, chartItem.pieColors[i]))

                if (chartItem.categoryWithCountItems.isNotEmpty()) {
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

            legendBinding.tvCategoryName.setTypeface(null, typeFace)
        }

        val entryIndex = binding.chart.data.dataSet.getEntryIndex(highlightedPieEntry)
        if (entryIndex >= 0) {
            binding.chart.highlightValue(entryIndex.toFloat(), 0f, 0)
        } else {
            binding.chart.highlightValues(arrayOf())
        }
    }
}
package com.kjh.mynote.ui.common.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ComponentsMyPieChartViewBinding
import com.kjh.mynote.databinding.LayoutHomePieChartLegendBinding
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 9..
 * Description:
 */
class MyPieChartView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentsMyPieChartViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentPieEntries: List<PieEntry>? = null
    private var currentLegendViews: List<View>? = null
    private var currentHighlightEntry: PieEntry? = null
    private var sliceClickListener: OnChartValueSelectedListener? = null

    init {
        initChart()
    }

    private fun initChart() = with (binding) {
        chart.apply {
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

            animateY(1000, Easing.EaseInOutCubic)
            animate()
        }
    }

    fun setupPieData(
        pieEntries: List<PieEntry>,
        pieColors: List<Int>,
        isEmptyData: Boolean
    ) {
        if (currentPieEntries == pieEntries) return

        currentPieEntries = pieEntries

        binding.chart.isHighlightPerTapEnabled = !isEmptyData
        binding.chart.data = PieData(
            PieDataSet(currentPieEntries, "").apply {
                colors = pieColors
                sliceSpace = 3f
                setDrawValues(false)
            })

        setupLegendView(currentPieEntries ?: emptyList(), pieColors)
    }

    fun setCenterText(text: String) = with (binding.chart) {
        if (centerText == text) return@with

        centerText = text
        invalidate()
    }

    fun setChartValueSelectedListener(listener: OnChartValueSelectedListener) {
        sliceClickListener = listener
        binding.chart.setOnChartValueSelectedListener(sliceClickListener)
    }

    fun updateHighlight(highlightedPieEntry: PieEntry?) {
        if (currentHighlightEntry == highlightedPieEntry) return

        currentHighlightEntry = highlightedPieEntry
        currentLegendViews?.forEach { view ->
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

    private fun setupLegendView(
        pieEntries: List<PieEntry>,
        pieColors: List<Int>,
    ) {
        binding.llLegendContainer.removeAllViews()
        currentLegendViews = makeLegendViews(pieEntries, pieColors)
        currentLegendViews?.let {
            it.forEach { binding.llLegendContainer.addView(it) }
        }
    }

    private fun makeLegendViews(
        pieEntries: List<PieEntry>,
        pieColors: List<Int>,
    ): List<View> =
        pieEntries.indices.map { index ->
            LayoutHomePieChartLegendBinding.inflate(
                LayoutInflater.from(context), binding.llLegendContainer, false
            ).apply {
                val legendName = pieEntries[index].label
                val legendColor = if (index <= 4) pieColors[index] else pieColors.last()

                tvLegendName.text = legendName
                (viewLegendColor.background as? GradientDrawable)?.setTint(legendColor)

                if (binding.chart.isHighlightPerTapEnabled) {
                    root.tag = legendName
                    root.addClickAnimation()
                    root.onThrottleClick {
                        sliceClickListener?.onValueSelected(pieEntries[index], null)
                    }
                }
            }.root
        }
}
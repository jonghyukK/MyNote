package com.kjh.mynote.ui.features.purchase.statistics.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
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
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.purchase.statistics.PurchaseNoteStaticsUiItem
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

/**
 * 구매노트 통계 화면 - 날짜 및 총 건수, 총 가격 데이터 ViewHolder.
 *
 * @property binding
 * @property dateClickAction
 */
class PurchaseNoteStatisticsInfoItemViewHolder(
    private val binding: VhPurchaseNoteStatsInfoItemBinding,
    private val dateClickAction: () -> Unit
): BaseViewHolder<PurchaseNoteStaticsUiItem.StatsInfoItem>(binding.root) {

    init {
        binding.clDate.onThrottleClick {
            bindItem?.let { dateClickAction() }
        }
    }

    override fun bind(item: PurchaseNoteStaticsUiItem.StatsInfoItem) {
        super.bind(item)

        with (binding) {
            tvDate.text = item.currentDate.toStringWithPattern("yyyy년 M월")
            tvTotalNoteCount.highlightText(
                fullText = "총 ${item.purchaseNoteTotalCount}건이고",
                wordToHighlight = item.purchaseNoteTotalCount.toString(),
                isBold = true
            )
            tvTotalNotePrice.highlightText(
                fullText = "총 금액은 ${item.purchaseNoteTotalPrice.toComma()}원이에요!",
                wordToHighlight = item.purchaseNoteTotalPrice.toComma(),
                isBold = true
            )
        }
    }
}

/**
 * 구매노트 통계 화면 - PieChart Item ViewHolder.
 *
 * @property binding
 * @property sliceClickAction
 */
class PieChartItemViewHolder(
    private val binding: VhPurchaseNoteStatsPieChartItemBinding,
    private val sliceClickAction: (PieEntry?) -> Unit,
): BaseViewHolder<PurchaseNoteStaticsUiItem.PieChartItem>(binding.root) {

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
    }

    override fun bind(item: PurchaseNoteStaticsUiItem.PieChartItem) {
        super.bind(item)

        setupPieChart(item)
        setupLegendView(item)
        updateHighlight(item.highlightedPieEntry)
    }

    private fun setupPieChart(item: PurchaseNoteStaticsUiItem.PieChartItem) {
        val pieDataSet = PieDataSet(item.pieEntries, "").apply {
            colors = item.pieColors.map { ContextCompat.getColor(context, it) }
            sliceSpace = 3f
            setDrawValues(false)
        }

        with (binding) {
            chart.centerText = item.highlightedPieEntry?.let { "구매노트 ${it.value.toInt()}건" }
            chart.isHighlightPerTapEnabled = !item.isEmpty
            chart.data = PieData(pieDataSet)
        }
    }

    private fun setupLegendView(item: PurchaseNoteStaticsUiItem.PieChartItem) {
        binding.llLegendContainer.removeAllViews()
        legendViews = makeLegendViews(item)
        legendViews.forEach { binding.llLegendContainer.addView(it) }
    }

    private fun makeLegendViews(item: PurchaseNoteStaticsUiItem.PieChartItem): List<View> {
        return item.pieEntries.indices.map { i ->
            val legendBinding = LayoutHomePieChartLegendBinding.inflate(
                LayoutInflater.from(context), binding.llLegendContainer, false)

            val categoryName = item.pieEntries[i].label
            val color = if (i <= 4) {
                item.pieColors[i]
            } else {
                item.pieColors.last()
            }

            with (legendBinding) {
                tvCategoryName.text = categoryName
                viewLegendColor.setBackgroundColor(
                    ContextCompat.getColor(context, color))

                if (!item.isEmpty) {
                    root.tag = categoryName
                    root.addClickAnimation()
                    root.onThrottleClick {
                        sliceClickAction(item.pieEntries[i])
                    }
                }
            }

            legendBinding.root
        }
    }

    private fun updateHighlight(highlightedPieEntry: PieEntry?) {
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

/**
 * 구매노트 통계 화면 - 카테고리 통계 목록 아이템 ViewHolder.
 *
 * @property binding
 */
class CategoryStatsItemViewHolder(
    private val binding: VhHomeCategoryPutchaseStatsItemBinding,
    private val categoryStatsClickAction: (CategoryStats) -> Unit
): BaseViewHolder<PurchaseNoteStaticsUiItem.CategoryStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryStatsClickAction(item.categoryStatsItem) }
        }
    }

    override fun bind(item: PurchaseNoteStaticsUiItem.CategoryStatsItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryStatsItem.categoryName
            tvCount.text = "구매노트 ${item.categoryStatsItem.purchaseNoteTotalCount}건"
            tvTotalPrice.text = context.getString(R.string.format_won, item.categoryStatsItem.purchaseNoteTotalPrice.toComma())
            vColor.setBackgroundColor(ContextCompat.getColor(context, item.color))
        }
    }
}
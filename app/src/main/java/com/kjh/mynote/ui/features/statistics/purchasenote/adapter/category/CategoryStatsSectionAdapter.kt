package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.databinding.VhPurchaseNoteStatsPieChartItemBinding
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.ui.features.statistics.purchasenote.CategoryStatsItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PieChartItem
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.parcelableArrayList

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */
class CategoryStatsSectionAdapter(
    private val categoryPieSliceClickAction: (PieEntry?) -> Unit,
    private val categoryStatsClickAction: (CategoryStatsUiModel) -> Unit,
    private val categoryStatsMoreClickAction: () -> Unit,
) : ListAdapter<PurchaseNoteStatisticsUiItemState.CategoryStatsSection, CategoryStatsSectionItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) =
        CategoryStatsSectionItemViewHolder(
            VhPurchaseNoteStatsPieChartItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            categoryPieSliceClickAction,
            categoryStatsClickAction,
            categoryStatsMoreClickAction
        )

    override fun onBindViewHolder(holder: CategoryStatsSectionItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: CategoryStatsSectionItemViewHolder,
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
                holder.updateCategoryStatsList(
                    isExpanded = bundle.getBoolean(BUNDLE_KEY_EXPANDED),
                    categoryStatsList = bundle.parcelableArrayList<CategoryStatsItem>(
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

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PurchaseNoteStatisticsUiItemState.CategoryStatsSection>() {
            override fun areItemsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
                newItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
            ): Boolean = oldItem::class == newItem::class

            override fun areContentsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
                newItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
                newItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
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
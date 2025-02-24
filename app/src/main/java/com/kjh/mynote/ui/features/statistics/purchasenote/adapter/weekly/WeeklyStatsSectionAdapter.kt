package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.weekly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteStatsWeeklySectionItemBinding
import com.kjh.mynote.model.WeeklyPurchaseNoteStatsUiModel
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
class WeeklyStatsSectionAdapter(
    private val weekStatsClickAction: (WeeklyPurchaseNoteStatsUiModel) -> Unit
) : ListAdapter<PurchaseNoteStatisticsUiItemState.WeeklyStatsSection, WeeklyStatsSectionItemViewHolder>(
        UI_MODEL_COMPARATOR
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) =
        WeeklyStatsSectionItemViewHolder(
            VhPurchaseNoteStatsWeeklySectionItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), weekStatsClickAction
        )

    override fun onBindViewHolder(holder: WeeklyStatsSectionItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PurchaseNoteStatisticsUiItemState.WeeklyStatsSection>() {
                override fun areItemsTheSame(
                    oldItem: PurchaseNoteStatisticsUiItemState.WeeklyStatsSection,
                    newItem: PurchaseNoteStatisticsUiItemState.WeeklyStatsSection,
                ): Boolean = oldItem::class == newItem::class

                override fun areContentsTheSame(
                    oldItem: PurchaseNoteStatisticsUiItemState.WeeklyStatsSection,
                    newItem: PurchaseNoteStatisticsUiItemState.WeeklyStatsSection,
                ): Boolean = oldItem == newItem
            }
    }
}
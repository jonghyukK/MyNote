package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.total

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */
class TotalStatsSectionAdapter(
    private val dateClickAction: () -> Unit,
) : ListAdapter<PurchaseNoteStatisticsUiItemState.StatsTotalSection, TotalStatsSectionItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) =
        TotalStatsSectionItemViewHolder(
            VhPurchaseNoteStatsInfoItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), dateClickAction
        )

    override fun onBindViewHolder(holder: TotalStatsSectionItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PurchaseNoteStatisticsUiItemState.StatsTotalSection>() {
            override fun areItemsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState.StatsTotalSection,
                newItem: PurchaseNoteStatisticsUiItemState.StatsTotalSection,
            ): Boolean = oldItem::class == newItem::class

            override fun areContentsTheSame(
                oldItem: PurchaseNoteStatisticsUiItemState.StatsTotalSection,
                newItem: PurchaseNoteStatisticsUiItemState.StatsTotalSection,
            ): Boolean = oldItem == newItem
        }
    }
}
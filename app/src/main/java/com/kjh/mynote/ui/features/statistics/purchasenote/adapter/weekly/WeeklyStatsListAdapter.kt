package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.weekly

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteStatsWeeklyListItemBinding
import com.kjh.mynote.model.WeeklyPurchaseNoteStatsUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
class WeeklyStatsListAdapter
    : ListAdapter<WeeklyPurchaseNoteStatsUiModel, WeeklyStatsListItemViewHolder>(UI_MODEL_COMPARATOR) {
        
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) =
        WeeklyStatsListItemViewHolder(
            VhPurchaseNoteStatsWeeklyListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: WeeklyStatsListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<WeeklyPurchaseNoteStatsUiModel>() {
            override fun areItemsTheSame(
                oldItem: WeeklyPurchaseNoteStatsUiModel,
                newItem: WeeklyPurchaseNoteStatsUiModel,
            ): Boolean = oldItem.weekLabel == newItem.weekLabel

            override fun areContentsTheSame(
                oldItem: WeeklyPurchaseNoteStatsUiModel,
                newItem: WeeklyPurchaseNoteStatsUiModel,
            ): Boolean = oldItem == newItem
        }
    }
}
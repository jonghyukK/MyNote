package com.kjh.mynote.ui.features.home.adapter.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryStats
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.ui.features.home.MonthlyCategoryStatsUiItem
import com.kjh.mynote.ui.features.home.adapter.weeklyplacenotes.HomePlaceNoteWeekViewInnerMoreItemViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
class HomeCategoryStatsListAdapter(
    private val categoryStatsItemClickAction: (CategoryStats) -> Unit,
    private val seeAllPurchaseStatsClickAction: () -> Unit
) : ListAdapter<MonthlyCategoryStatsUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = when (viewType) {
        VIEW_TYPE_CATEGORY_STATS -> {
            HomeCategoryStatsItemViewHolder(
                VhHomeCategoryPutchaseStatsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), categoryStatsItemClickAction
            )
        }
        VIEW_TYPE_MORE -> {
            HomeCategoryStatsMoreItemViewHolder(
                VhHomePlaceNoteWeekViewInnerMoreItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), seeAllPurchaseStatsClickAction
            )
        }
        else -> throw Exception("Wrong ViewType: $viewType")
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is MonthlyCategoryStatsUiItem.CategoryStatsItem ->
                (holder as HomeCategoryStatsItemViewHolder).bind(item)

            is MonthlyCategoryStatsUiItem.More ->
                (holder as HomeCategoryStatsMoreItemViewHolder).bind(Unit)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is MonthlyCategoryStatsUiItem.CategoryStatsItem -> VIEW_TYPE_CATEGORY_STATS
        is MonthlyCategoryStatsUiItem.More -> VIEW_TYPE_MORE
    }

    companion object {
        private const val VIEW_TYPE_CATEGORY_STATS = 1
        private const val VIEW_TYPE_MORE = 2

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<MonthlyCategoryStatsUiItem>() {
                override fun areItemsTheSame(
                    oldItem: MonthlyCategoryStatsUiItem,
                    newItem: MonthlyCategoryStatsUiItem,
                ): Boolean = when {
                    oldItem is MonthlyCategoryStatsUiItem.CategoryStatsItem &&
                            newItem is MonthlyCategoryStatsUiItem.CategoryStatsItem -> {
                        oldItem.item.categoryId == newItem.item.categoryId
                    }

                    oldItem is MonthlyCategoryStatsUiItem.More &&
                            newItem is MonthlyCategoryStatsUiItem.More -> true

                    else -> false
                }

                override fun areContentsTheSame(
                    oldItem: MonthlyCategoryStatsUiItem,
                    newItem: MonthlyCategoryStatsUiItem,
                ): Boolean = oldItem == newItem
            }
    }
}
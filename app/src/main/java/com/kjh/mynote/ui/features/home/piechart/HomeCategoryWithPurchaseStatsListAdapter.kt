package com.kjh.mynote.ui.features.home.piechart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CategoryWithPurchaseNotesCountAndTotalPrice
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.ui.features.home.HomeCategoryStatsUiState
import com.kjh.mynote.ui.features.home.weekview.HomePlaceNoteWeekViewInnerMoreItemViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
class HomeCategoryWithPurchaseStatsListAdapter(
    private val categoryStatsItemClickAction: (CategoryWithPurchaseNotesCountAndTotalPrice) -> Unit
) : ListAdapter<HomeCategoryStatsUiState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = when (viewType) {
        VIEW_TYPE_CATEGORY_STATS -> {
            HomeCategoryWithPurchaseStatsItemViewHolder(
                VhHomeCategoryPutchaseStatsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), categoryStatsItemClickAction
            )
        }
        VIEW_TYPE_MORE -> {
            HomePlaceNoteWeekViewInnerMoreItemViewHolder(
                VhHomePlaceNoteWeekViewInnerMoreItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        else -> throw Exception("Wrong ViewType: $viewType")
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is HomeCategoryStatsUiState.CategoryWithStatsItem ->
                (holder as HomeCategoryWithPurchaseStatsItemViewHolder).bind(item)

            is HomeCategoryStatsUiState.MoreItem ->
                (holder as HomePlaceNoteWeekViewInnerMoreItemViewHolder).bind(Unit)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeCategoryStatsUiState.CategoryWithStatsItem -> VIEW_TYPE_CATEGORY_STATS
        is HomeCategoryStatsUiState.MoreItem -> VIEW_TYPE_MORE
    }

    companion object {
        private const val VIEW_TYPE_CATEGORY_STATS = 1
        private const val VIEW_TYPE_MORE = 2

        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<HomeCategoryStatsUiState>() {
                override fun areItemsTheSame(
                    oldItem: HomeCategoryStatsUiState,
                    newItem: HomeCategoryStatsUiState,
                ): Boolean = when {
                    oldItem is HomeCategoryStatsUiState.CategoryWithStatsItem &&
                            newItem is HomeCategoryStatsUiState.CategoryWithStatsItem -> {
                        oldItem.categoryStatsItem.categoryId == newItem.categoryStatsItem.categoryId
                    }

                    oldItem is HomeCategoryStatsUiState.MoreItem &&
                            newItem is HomeCategoryStatsUiState.MoreItem -> true

                    else -> false
                }

                override fun areContentsTheSame(
                    oldItem: HomeCategoryStatsUiState,
                    newItem: HomeCategoryStatsUiState,
                ): Boolean = oldItem == newItem
            }
    }
}
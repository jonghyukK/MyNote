package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.domain.model.CategoryStats
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.ui.features.statistics.purchasenote.CategoryStatsItem

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 19..
 * Description:
 */
class CategoryStatsListAdapter(
    private val categoryStatsClickAction: (CategoryStatsUiModel) -> Unit
): ListAdapter<CategoryStatsItem, CategoryStatsListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryStatsListItemViewHolder(
            VhHomeCategoryPutchaseStatsItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), categoryStatsClickAction
        )

    override fun onBindViewHolder(holder: CategoryStatsListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<CategoryStatsItem>() {
                override fun areItemsTheSame(
                    oldItem: CategoryStatsItem,
                    newItem: CategoryStatsItem,
                ): Boolean =
                    oldItem.categoryStatsItem.categoryId == newItem.categoryStatsItem.categoryId

                override fun areContentsTheSame(
                    oldItem: CategoryStatsItem,
                    newItem: CategoryStatsItem,
                ): Boolean = oldItem == newItem
            }
    }
}


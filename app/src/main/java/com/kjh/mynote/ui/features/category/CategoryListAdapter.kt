package com.kjh.mynote.ui.features.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhCategoryListItemBinding
import com.kjh.mynote.model.CategoryUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class CategoryListAdapter(
    private val onItemClickAction: (CategoryUiModel) -> Unit,
    private val onEditClickAction: (CategoryUiModel) -> Unit,
    private val onDeleteClickAction: (CategoryUiModel) -> Unit
): ListAdapter<CategoryListItem, CategoryListItemViewHolder>(UI_MODEL_COMPARATOR) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryListItemViewHolder(
            VhCategoryListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onItemClickAction, onEditClickAction, onDeleteClickAction
        )

    override fun onBindViewHolder(holder: CategoryListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<CategoryListItem>() {
            override fun areItemsTheSame(
                oldItem: CategoryListItem,
                newItem: CategoryListItem
            ): Boolean =
                oldItem.isSelected == newItem.isSelected
                        && oldItem.categoryItem.id == newItem.categoryItem.id

            override fun areContentsTheSame(
                oldItem: CategoryListItem,
                newItem: CategoryListItem
            ): Boolean = oldItem == newItem
        }
    }
}
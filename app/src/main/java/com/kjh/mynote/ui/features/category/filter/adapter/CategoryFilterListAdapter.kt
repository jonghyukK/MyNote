package com.kjh.mynote.ui.features.category.filter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.features.category.list.CategoryListItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */
class CategoryFilterListAdapter(
    private val onItemClickAction: (CategoryUiModel) -> Unit
): ListAdapter<CategoryListItem, CategoryFilterListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CategoryFilterListItemViewHolder(
            VhFilterCategoryItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onItemClickAction
        )

    override fun onBindViewHolder(holder: CategoryFilterListItemViewHolder, position: Int) {
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
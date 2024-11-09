package com.kjh.mynote.ui.features.category

import com.kjh.mynote.databinding.VhCategoryListItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class CategoryListItemViewHolder(
    private val binding: VhCategoryListItemBinding,
    private val onItemClickAction: (CategoryUiModel) -> Unit,
    private val onEditClickAction: (CategoryUiModel) -> Unit,
    private val onDeleteClickAction: (CategoryUiModel) -> Unit
): BaseViewHolder<CategoryUiModel>(binding.root) {

    init {
        binding.root.onThrottleClick {
            bindItem?.let { item -> onItemClickAction.invoke(item) }
        }

        binding.ivEdit.onThrottleClick {
            bindItem?.let { item -> onEditClickAction.invoke(item)}
        }

        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> onDeleteClickAction.invoke(item)}
        }
    }

    override fun bind(item: CategoryUiModel) {
        super.bind(item)

        binding.tvCategoryName.text = item.categoryName
    }
}
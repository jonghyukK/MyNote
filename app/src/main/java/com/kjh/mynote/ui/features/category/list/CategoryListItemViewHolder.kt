package com.kjh.mynote.ui.features.category.list

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhCategoryListItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

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
): BaseViewHolder<CategoryListItem>(binding.root) {

    init {
        binding.root.onThrottleClick {
            bindItem?.let { item -> onItemClickAction.invoke(item.categoryItem) }
        }

        binding.ivEdit.onThrottleClick {
            bindItem?.let { item -> onEditClickAction.invoke(item.categoryItem)}
        }

        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> onDeleteClickAction.invoke(item.categoryItem)}
        }
    }

    override fun bind(item: CategoryListItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryItem.categoryName

            if (item.isSelected) {
                ivChecked.isVisible = true
                tvCategoryName.setTextColorRes(R.color.purple)
            } else {
                ivChecked.isVisible = false
                tvCategoryName.setTextColorRes(R.color.black_700)
            }

            ivEdit.isVisible = !item.isDefaultCategory
            ivDelete.isVisible = !item.isDefaultCategory
        }
    }
}
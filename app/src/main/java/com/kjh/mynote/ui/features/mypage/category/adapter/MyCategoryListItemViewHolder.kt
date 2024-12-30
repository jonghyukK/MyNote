package com.kjh.mynote.ui.features.mypage.category.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.databinding.VhMyCategoryListItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

class MyCategoryListItemViewHolder(
    private val binding: VhMyCategoryListItemBinding,
    private val editClickAction: (CategoryUiModel) -> Unit,
    private val deleteClickAction: (CategoryUiModel) -> Unit
): BaseViewHolder<CategoryUiModel>(binding.root) {

    init {
        binding.ivEdit.onThrottleClick {
            bindItem?.let { item -> editClickAction(item) }
        }

        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> deleteClickAction(item) }
        }
    }

    override fun bind(item: CategoryUiModel) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryName
            ivEdit.isVisible = !item.isDefaultCategory()
            ivDelete.isVisible = !item.isDefaultCategory()
        }
    }
}
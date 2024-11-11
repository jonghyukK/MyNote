package com.kjh.mynote.ui.features.category.filter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.category.list.CategoryListItem
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */

class CategoryFilterListItemViewHolder(
    private val binding: VhFilterCategoryItemBinding,
    private val onItemClickAction: (CategoryUiModel) -> Unit
): BaseViewHolder<CategoryListItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onItemClickAction.invoke(item.categoryItem) }
        }
    }

    override fun bind(item: CategoryListItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryItem.categoryName

            if (item.isSelected) {
                root.background = context.getDrawableCompat(R.drawable.shape_s_purple_c_20)
                tvCategoryName.setTextColorRes(R.color.white)
            } else {
                root.background = context.getDrawableCompat(R.drawable.shape_s_white_c_20_l_purple)
                tvCategoryName.setTextColorRes(R.color.purple)
            }
        }
    }
}
package com.kjh.mynote.ui.features.category.list.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhCategoryOrPaymentMethodListItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.category.list.CategoryListItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class CategoryListItemViewHolder(
    private val binding: VhCategoryOrPaymentMethodListItemBinding,
    private val onItemClickAction: (CategoryUiModel) -> Unit
): BaseViewHolder<CategoryListItem>(binding.root) {

    init {
        binding.root.onThrottleClick {
            bindItem?.let { item -> onItemClickAction.invoke(item.categoryItem) }
        }
    }

    override fun bind(item: CategoryListItem) {
        super.bind(item)

        val nameColor = if (item.isSelected) R.color.colorPrimary else R.color.black_900

        with (binding) {
            ivChecked.isVisible = item.isSelected

            tvName.text = item.categoryItem.categoryName
            tvName.setTextColorRes(nameColor)

            tvCount.text = item.categoryItem.purchaseNoteCount.toString()
        }
    }
}
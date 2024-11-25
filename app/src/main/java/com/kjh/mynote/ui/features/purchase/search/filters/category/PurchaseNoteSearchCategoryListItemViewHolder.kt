package com.kjh.mynote.ui.features.purchase.search.filters.category

import android.graphics.Typeface
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchCategoryListItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.category.list.CategoryListItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 25..
 * Description:
 */

class PurchaseNoteSearchCategoryListItemViewHolder(
    private val binding: VhPurchaseNoteSearchCategoryListItemBinding,
    private val categoryClickAction: (Int) -> Unit
): BaseViewHolder<CategoryListItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryClickAction(item.categoryItem.id) }
        }
    }

    override fun bind(item: CategoryListItem) {
        super.bind(item)

        with (binding) {
            tvFilterName.text = item.categoryItem.categoryName
            if (item.isSelected) {
                tvFilterName.setTypeface(null, Typeface.BOLD)
                tvFilterName.setTextColorRes(appliedTextColor)
            } else {
                tvFilterName.setTypeface(null, Typeface.NORMAL)
                tvFilterName.setTextColorRes(normalTextColor)
            }
            ivImage.isVisible = item.isSelected
        }
    }

    companion object {
        private val appliedTextColor = R.color.purple
        private val normalTextColor = R.color.black_600
    }
}
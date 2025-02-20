package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.category

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.CategoryStatsItem
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

/**
 * 구매노트 통계 화면 - 카테고리 통계 목록 아이템 ViewHolder.
 *
 * @property binding
 */
class CategoryStatsListItemViewHolder(
    private val binding: VhHomeCategoryPutchaseStatsItemBinding,
    private val categoryStatsClickAction: (CategoryStatsUiModel) -> Unit,
) : BaseViewHolder<CategoryStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryStatsClickAction(item.categoryStatsItem) }
        }
    }

    override fun bind(item: CategoryStatsItem) {
        super.bind(item)

        with(binding) {
            tvCategoryName.text = item.categoryStatsItem.categoryName
            tvCount.text = "구매노트 ${item.categoryStatsItem.purchaseNoteTotalCount}건"
            tvTotalPrice.text = context.getString(
                R.string.format_won,
                item.categoryStatsItem.purchaseNoteTotalPrice.toComma()
            )
            (vColor.background as? GradientDrawable)?.setTint(ContextCompat.getColor(context, item.color))
        }
    }
}
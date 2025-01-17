package com.kjh.mynote.ui.features.purchase.statistics.adapter.section.contents

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.example.domain.model.CategoryStats
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.StatsContentsItem
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
class CategoryStatsItemViewHolder(
    private val binding: VhHomeCategoryPutchaseStatsItemBinding,
    private val categoryStatsClickAction: (CategoryStats) -> Unit,
) : BaseViewHolder<StatsContentsItem.CategoryStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryStatsClickAction(item.categoryStatsItem) }
        }
    }

    override fun bind(item: StatsContentsItem.CategoryStatsItem) {
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
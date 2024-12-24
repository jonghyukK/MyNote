package com.kjh.mynote.ui.features.home.adapter.statistics

import androidx.core.content.ContextCompat
import com.example.domain.model.CategoryStats
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.home.MonthlyCategoryStatsUiItem
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
class HomeCategoryStatsItemViewHolder(
    private val binding: VhHomeCategoryPutchaseStatsItemBinding,
    private val categoryStatsItemClickAction: (CategoryStats) -> Unit
): BaseViewHolder<MonthlyCategoryStatsUiItem.CategoryStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryStatsItemClickAction(item.item) }
        }
    }

    override fun bind(item: MonthlyCategoryStatsUiItem.CategoryStatsItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.item.categoryName
            tvCount.text = "구매노트 ${item.item.purchaseNoteTotalCount}건"
            tvTotalPrice.text = context.getString(R.string.format_won, item.item.purchaseNoteTotalPrice.toComma())
            vColor.setBackgroundColor(ContextCompat.getColor(context, item.color))
        }
    }
}

class HomeCategoryStatsMoreItemViewHolder(
    private val binding: VhHomePlaceNoteWeekViewInnerMoreItemBinding,
    private val seeAllPurchaseStatsClickAction: () -> Unit
): BaseViewHolder<Unit>(binding.root) {

    init {
        binding.tvMore.addClickAnimation()
        binding.tvMore.onThrottleClick {
            bindItem?.let { seeAllPurchaseStatsClickAction() }
        }
    }
}
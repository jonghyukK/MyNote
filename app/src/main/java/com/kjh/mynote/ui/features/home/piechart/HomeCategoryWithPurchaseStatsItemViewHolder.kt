package com.kjh.mynote.ui.features.home.piechart

import androidx.core.content.ContextCompat
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.home.HomeCategoryStatsUiState
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
class HomeCategoryWithPurchaseStatsItemViewHolder(
    private val binding: VhHomeCategoryPutchaseStatsItemBinding
): BaseViewHolder<HomeCategoryStatsUiState.CategoryWithStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {

        }
    }

    override fun bind(item: HomeCategoryStatsUiState.CategoryWithStatsItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryStatsItem.categoryName
            tvCount.text = "구매노트 ${item.categoryStatsItem.purchaseNoteCount}건"
            tvTotalPrice.text = context.getString(R.string.format_won, item.categoryStatsItem.totalPurchasePrice.toComma())
            vColor.setBackgroundColor(ContextCompat.getColor(context, item.colors))
        }
    }
}
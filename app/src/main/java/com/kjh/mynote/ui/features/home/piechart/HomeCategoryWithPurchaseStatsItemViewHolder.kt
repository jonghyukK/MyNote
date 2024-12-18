package com.kjh.mynote.ui.features.home.piechart

import androidx.core.content.ContextCompat
import com.example.domain.model.CategoryWithStats
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
    private val binding: VhHomeCategoryPutchaseStatsItemBinding,
    private val categoryStatsItemClickAction: (CategoryWithStats) -> Unit
): BaseViewHolder<HomeCategoryStatsUiState.CategoryWithStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryStatsItemClickAction(item.categoryWithStatsItem) }
        }
    }

    override fun bind(item: HomeCategoryStatsUiState.CategoryWithStatsItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryWithStatsItem.categoryName
            tvCount.text = "구매노트 ${item.categoryWithStatsItem.purchaseNoteTotalCount}건"
            tvTotalPrice.text = context.getString(R.string.format_won, item.categoryWithStatsItem.purchaseNoteTotalPrice.toComma())
            vColor.setBackgroundColor(ContextCompat.getColor(context, item.colors))
        }
    }
}
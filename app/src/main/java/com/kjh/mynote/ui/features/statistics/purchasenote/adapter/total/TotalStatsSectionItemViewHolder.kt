package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.total

import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

/**
 * 구매노트 통계 화면 - 날짜 및 총 건수, 총 가격 데이터 ViewHolder.
 *
 * @property binding
 * @property dateClickAction
 */
class TotalStatsSectionItemViewHolder(
    private val binding: VhPurchaseNoteStatsInfoItemBinding,
    private val dateClickAction: () -> Unit
): BaseViewHolder<PurchaseNoteStatisticsUiItemState.StatsTotalSection>(binding.root) {

    init {
        binding.clDate.onThrottleClick {
            bindItem?.let { dateClickAction() }
        }
    }

    override fun bind(item: PurchaseNoteStatisticsUiItemState.StatsTotalSection) {
        super.bind(item)

        with (binding) {
            tvDate.text = item.currentDate.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M)
            tvTotalPrice.text = "${item.purchaseNoteTotalPrice.toComma()}원"
            tvTotalCount.text = " / ${item.purchaseNoteTotalCount}건"
        }
    }
}
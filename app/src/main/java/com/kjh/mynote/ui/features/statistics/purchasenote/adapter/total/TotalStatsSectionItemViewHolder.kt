package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.total

import androidx.core.view.isVisible
import com.kjh.mynote.databinding.VhPurchaseNoteStatsInfoItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.PurchaseNoteStatisticsUiItemState
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma
import java.time.LocalDate

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
    private val dateClickAction: () -> Unit,
    private val prevMonthClickAction: (LocalDate) -> Unit,
    private val nextMonthClickAction: (LocalDate) -> Unit
): BaseViewHolder<PurchaseNoteStatisticsUiItemState.StatsTotalSection>(binding.root) {

    init {
        binding.tvDate.onThrottleClick {
            bindItem?.let { dateClickAction() }
        }

        binding.ivPrevMonth.onThrottleClick {
            bindItem?.let {
                val prevMonth = it.currentDate.minusMonths(1)
                prevMonthClickAction(prevMonth)
            }
        }

        binding.ivNextMonth.onThrottleClick {
            bindItem?.let {
                val nextMonth = it.currentDate.plusMonths(1)
                nextMonthClickAction(nextMonth)
            }
        }
    }

    override fun bind(item: PurchaseNoteStatisticsUiItemState.StatsTotalSection) {
        super.bind(item)

        with (binding) {
            tvDate.text = item.currentDateUiText
            ivNextMonth.isVisible = item.showNextMonthBtn
            tvTotalPrice.text = "${item.purchaseNoteTotalPrice.toComma()}원"
            tvTotalCount.text = "${item.purchaseNoteTotalCount}건"
        }
    }
}
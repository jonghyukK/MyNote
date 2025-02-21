package com.kjh.mynote.ui.common.dialog.yearmonths.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhSelectableYearMonthListItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthItem
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toStringWithPattern
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 20..
 * Description:
 */

class SelectableYearMonthItemViewHolder(
    private val binding: VhSelectableYearMonthListItemBinding,
    private val onItemClickAction: (LocalDate) -> Unit
): BaseViewHolder<SelectableYearMonthItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onItemClickAction.invoke(item.date) }
        }
    }

    override fun bind(item: SelectableYearMonthItem) {
        super.bind(item)

        val textColor = if (item.isSelected) R.color.purple else R.color.black_800

        with (binding) {
            tvYearMonth.text = item.date.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M)
            tvYearMonth.setTextColorRes(textColor)

            ivCheck.isVisible = item.isSelected
        }
    }
}
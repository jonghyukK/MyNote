package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.paymentmethod

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.purchasenote.PaymentMethodStatsItem
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

/**
 * 구매노트 통계 화면 - 구매노트 통계 목록 아이템 ViewHolder.
 *
 * @property binding
 */
class PaymentMethodStatsListItemViewHolder(
    private val binding: VhHomeCategoryPutchaseStatsItemBinding,
    private val paymentMethodStatsClickAction: (PaymentMethodStatsUiModel) -> Unit,
) : BaseViewHolder<PaymentMethodStatsItem>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> paymentMethodStatsClickAction(item.paymentMethodStatsItem) }
        }
    }

    override fun bind(item: PaymentMethodStatsItem) {
        super.bind(item)

        with(binding) {
            tvCategoryName.text = item.paymentMethodStatsItem.paymentMethodName
            tvCount.text = "구매노트 ${item.paymentMethodStatsItem.purchaseNoteTotalCount}건"
            tvTotalPrice.text = context.getString(
                R.string.format_won,
                item.paymentMethodStatsItem.purchaseNoteTotalPrice.toComma()
            )
            (vColor.background as? GradientDrawable)?.setTint(ContextCompat.getColor(context, item.color))
        }
    }
}
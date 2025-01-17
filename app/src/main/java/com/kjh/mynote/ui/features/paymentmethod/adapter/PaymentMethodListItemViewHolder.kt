package com.kjh.mynote.ui.features.paymentmethod.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhCategoryOrPaymentMethodListItemBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.paymentmethod.SelectablePaymentMethodItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */

class PaymentMethodListItemViewHolder(
    private val binding: VhCategoryOrPaymentMethodListItemBinding,
    private val paymentMethodItemClickAction: (PaymentMethodUiModel) -> Unit
): BaseViewHolder<SelectablePaymentMethodItem>(binding.root) {

    init {
        binding.root.onThrottleClick {
            bindItem?.let { item -> paymentMethodItemClickAction(item.paymentMethodItem) }
        }
    }

    override fun bind(item: SelectablePaymentMethodItem) {
        super.bind(item)

        with (binding) {
            ivChecked.isVisible = item.isSelected
            tvCount.isVisible = false

            val nameColorRes = if (item.isSelected) R.color.colorPrimary else R.color.black_900
            tvName.setTextColorRes(nameColorRes)
            tvName.text = item.paymentMethodItem.paymentMethodName
        }
    }
}
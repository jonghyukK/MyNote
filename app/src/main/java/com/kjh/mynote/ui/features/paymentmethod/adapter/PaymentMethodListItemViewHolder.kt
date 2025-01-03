package com.kjh.mynote.ui.features.paymentmethod.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhMyPaymentMethodListItemBinding
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
    private val binding: VhMyPaymentMethodListItemBinding,
    private val paymentMethodItemClickAction: (PaymentMethodUiModel) -> Unit,
    private val editPaymentMethodClickAction: (PaymentMethodUiModel) -> Unit
): BaseViewHolder<SelectablePaymentMethodItem>(binding.root) {

    init {
        binding.tvName.onThrottleClick {
            bindItem?.let { item -> paymentMethodItemClickAction(item.paymentMethodItem) }
        }

        binding.ivEdit.onThrottleClick {
            bindItem?.let { item -> editPaymentMethodClickAction(item.paymentMethodItem)}
        }
    }

    override fun bind(item: SelectablePaymentMethodItem) {
        super.bind(item)

        with (binding) {
            ivCheck.isVisible = item.isSelected

            val nameColorRes = if (item.isSelected) R.color.colorPrimary else R.color.black_900
            tvName.setTextColorRes(nameColorRes)

            tvName.text = item.paymentMethodItem.paymentMethodName
            ivEdit.isVisible = item.paymentMethodItem.isDefaultMethod().not()
            ivDelete.isVisible = false
        }
    }
}
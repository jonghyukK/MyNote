package com.kjh.mynote.ui.features.mypage.paymentmethod.manage.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.databinding.VhMyPaymentMethodListItemBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
class PaymentMethodListItemViewHolder(
    private val binding: VhMyPaymentMethodListItemBinding,
    private val editClickAction: (PaymentMethodUiModel) -> Unit,
    private val deleteClickAction: (PaymentMethodUiModel) -> Unit
): BaseViewHolder<PaymentMethodUiModel>(binding.root) {

    init {
        binding.ivEdit.onThrottleClick {
            bindItem?.let { item -> editClickAction(item) }
        }

        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> deleteClickAction(item) }
        }
    }

    override fun bind(item: PaymentMethodUiModel) {
        super.bind(item)

        with (binding) {
            ivCheck.isVisible = false
            tvName.text = item.paymentMethodName
            ivEdit.isVisible = item.isDefaultMethod().not()
            ivDelete.isVisible = item.isDefaultMethod().not()
        }
    }
}
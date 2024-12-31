package com.kjh.mynote.ui.features.mypage.paymentmethod.manage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhMyPaymentMethodListItemBinding
import com.kjh.mynote.model.PaymentMethodUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
class PaymentMethodManageListAdapter(
    private val editClickAction: (PaymentMethodUiModel) -> Unit,
    private val deleteClickAction: (PaymentMethodUiModel) -> Unit
): ListAdapter<PaymentMethodUiModel, PaymentMethodListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PaymentMethodListItemViewHolder(
        VhMyPaymentMethodListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), editClickAction, deleteClickAction
    )

    override fun onBindViewHolder(holder: PaymentMethodListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PaymentMethodUiModel>() {
            override fun areItemsTheSame(
                oldItem: PaymentMethodUiModel,
                newItem: PaymentMethodUiModel
            ): Boolean = oldItem.paymentMethodId == newItem.paymentMethodId

            override fun areContentsTheSame(
                oldItem: PaymentMethodUiModel,
                newItem: PaymentMethodUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
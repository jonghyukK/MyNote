package com.kjh.mynote.ui.features.paymentmethod.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhMyPaymentMethodListItemBinding
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.features.paymentmethod.SelectablePaymentMethodItem

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */
class PaymentMethodListAdapter(
    private val paymentMethodItemClickAction: (PaymentMethodUiModel) -> Unit,
    private val editPaymentMethodClickAction: (PaymentMethodUiModel) -> Unit
): ListAdapter<SelectablePaymentMethodItem, PaymentMethodListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PaymentMethodListItemViewHolder(
        VhMyPaymentMethodListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), paymentMethodItemClickAction, editPaymentMethodClickAction
    )

    override fun onBindViewHolder(holder: PaymentMethodListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<SelectablePaymentMethodItem>() {
            override fun areItemsTheSame(
                oldItem: SelectablePaymentMethodItem,
                newItem: SelectablePaymentMethodItem
            ): Boolean = oldItem.paymentMethodItem.paymentMethodId ==
                    newItem.paymentMethodItem.paymentMethodId

            override fun areContentsTheSame(
                oldItem: SelectablePaymentMethodItem,
                newItem: SelectablePaymentMethodItem
            ): Boolean = oldItem == newItem
        }
    }
}
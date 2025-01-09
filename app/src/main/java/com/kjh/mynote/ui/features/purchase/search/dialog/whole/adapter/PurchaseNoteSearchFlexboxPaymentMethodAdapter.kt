package com.kjh.mynote.ui.features.purchase.search.dialog.whole.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.model.Filters

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 6..
 * Description:
 */
class PurchaseNoteSearchFlexboxPaymentMethodAdapter(
    private val paymentMethodItemClickAction: (Filters.PaymentMethod) -> Unit
): ListAdapter<Filters.PaymentMethod, PurchaseNoteSearchFlexboxPaymentMethodItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PurchaseNoteSearchFlexboxPaymentMethodItemViewHolder(
        VhFilterCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), paymentMethodItemClickAction
    )

    override fun onBindViewHolder(
        holder: PurchaseNoteSearchFlexboxPaymentMethodItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<Filters.PaymentMethod>() {
                override fun areItemsTheSame(
                    oldItem: Filters.PaymentMethod,
                    newItem: Filters.PaymentMethod
                ): Boolean = oldItem.paymentMethod.paymentMethodId == newItem.paymentMethod.paymentMethodId

                override fun areContentsTheSame(
                    oldItem: Filters.PaymentMethod,
                    newItem: Filters.PaymentMethod
                ): Boolean = oldItem == newItem
            }
    }
}

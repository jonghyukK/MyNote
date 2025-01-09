package com.kjh.mynote.ui.features.purchase.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteSearchCategoryListItemBinding
import com.kjh.mynote.model.Filters

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 6..
 * Description:
 */
class PurchaseNoteSearchPaymentMethodListAdapter(
    private val paymentMethodFilterClickAction: (Filters.PaymentMethod) -> Unit
): ListAdapter<Filters.PaymentMethod, PurchaseNoteSearchPaymentMethodListItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PurchaseNoteSearchPaymentMethodListItemViewHolder(
        VhPurchaseNoteSearchCategoryListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), paymentMethodFilterClickAction
    )
    override fun onBindViewHolder(
        holder: PurchaseNoteSearchPaymentMethodListItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<Filters.PaymentMethod>() {
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
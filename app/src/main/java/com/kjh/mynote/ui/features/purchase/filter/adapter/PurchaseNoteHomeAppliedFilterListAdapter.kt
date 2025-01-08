package com.kjh.mynote.ui.features.purchase.filter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteHomeAppliedFilterItemBinding
import com.kjh.mynote.model.Filters

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 8..
 * Description:
 */
class PurchaseNoteHomeAppliedFilterListAdapter(
    private val appliedFilterClickAction: (Filters) -> Unit
): ListAdapter<Filters, PurchaseNoteHomeAppliedFilterItemViewHolder>(UI_MODEL_COMPARATOR) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = PurchaseNoteHomeAppliedFilterItemViewHolder(
        VhPurchaseNoteHomeAppliedFilterItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), appliedFilterClickAction
    )

    override fun onBindViewHolder(
        holder: PurchaseNoteHomeAppliedFilterItemViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<Filters>() {
            override fun areItemsTheSame(
                oldItem: Filters,
                newItem: Filters
            ): Boolean = when {
                oldItem is Filters.Category && newItem is Filters.Category -> {
                    oldItem.categoryItem.id == newItem.categoryItem.id
                }
                oldItem is Filters.PaymentMethod && newItem is Filters.PaymentMethod -> {
                    oldItem.paymentMethod.paymentMethodId == newItem.paymentMethod.paymentMethodId
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: Filters,
                newItem: Filters
            ): Boolean = oldItem == newItem
        }
    }
}
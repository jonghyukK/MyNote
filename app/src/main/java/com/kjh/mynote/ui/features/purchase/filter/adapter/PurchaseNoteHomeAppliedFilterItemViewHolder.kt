package com.kjh.mynote.ui.features.purchase.filter.adapter

import com.kjh.mynote.databinding.VhPurchaseNoteHomeAppliedFilterItemBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 8..
 * Description:
 */
class PurchaseNoteHomeAppliedFilterItemViewHolder(
    private val binding: VhPurchaseNoteHomeAppliedFilterItemBinding,
    private val appliedFilterClickAction: (Filters) -> Unit
): BaseViewHolder<Filters>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> appliedFilterClickAction(item) }
        }
    }

    override fun bind(item: Filters) {
        super.bind(item)

        binding.tvName.text = when (item) {
            is Filters.Category -> item.categoryItem.categoryName
            is Filters.PaymentMethod ->item.paymentMethod.paymentMethodName
            else -> ""
        }
    }
}
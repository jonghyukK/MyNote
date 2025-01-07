package com.kjh.mynote.ui.features.purchase.search.adapter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchSelectedFilterItemBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 26..
 * Description:
 */

class PurchaseNoteSearchSelectedFilterItemViewHolder(
    private val binding: VhPurchaseNoteSearchSelectedFilterItemBinding,
    private val filterClickAction: (Filters) -> Unit
): BaseViewHolder<Filters>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> filterClickAction(item) }
        }
    }

    override fun bind(item: Filters) {
        super.bind(item)

        when (item) {
            is Filters.Category -> {
                binding.tvFilterName.text = item.categoryItem.categoryName
            }
            is Filters.PaymentMethod -> {
                binding.tvFilterName.text = item.paymentMethod.paymentMethodName
            }
            is Filters.DateRange -> {
                binding.tvFilterName.text = item.dateRangeFilter.getUiText()
            }
            is Filters.Price -> {
                binding.tvFilterName.text = if (item.minPrice != null && item.maxPrice == null) {
                    context.getString(R.string.format_won_up, item.minPrice.toComma())
                } else if (item.minPrice == null && item.maxPrice != null) {
                    context.getString(R.string.format_won_below, item.maxPrice.toComma())
                } else {
                    context.getString(
                        R.string.format_min_price_until_max_price,
                        item.minPrice?.toComma() ?: "0",
                        item.maxPrice?.toComma() ?: item.myMaxPrice.toComma()
                    )
                }
            }
            is Filters.PurchaseName -> {
                binding.tvFilterName.text = item.purchaseName
            }
        }
    }
}
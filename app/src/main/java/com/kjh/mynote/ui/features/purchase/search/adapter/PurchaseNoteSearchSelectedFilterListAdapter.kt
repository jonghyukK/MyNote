package com.kjh.mynote.ui.features.purchase.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteSearchSelectedFilterItemBinding
import com.kjh.mynote.model.Filters

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 26..
 * Description:
 */
class PurchaseNoteSearchSelectedFilterListAdapter(
    private val filterClickAction: (Filters) -> Unit
): ListAdapter<Filters, PurchaseNoteSearchSelectedFilterItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PurchaseNoteSearchSelectedFilterItemViewHolder(
        VhPurchaseNoteSearchSelectedFilterItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), filterClickAction
    )

    override fun onBindViewHolder(
        holder: PurchaseNoteSearchSelectedFilterItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<Filters>() {
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
                    oldItem is Filters.PurchaseName && newItem is Filters.PurchaseName -> {
                        oldItem.purchaseName == newItem.purchaseName
                    }
                    oldItem is Filters.DateRange && newItem is Filters.DateRange -> {
                        oldItem.dateRangeFilter == newItem.dateRangeFilter
                    }
                    oldItem is Filters.Price && newItem is Filters.Price -> {
                        oldItem.minPrice == newItem.minPrice && oldItem.maxPrice == newItem.maxPrice
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
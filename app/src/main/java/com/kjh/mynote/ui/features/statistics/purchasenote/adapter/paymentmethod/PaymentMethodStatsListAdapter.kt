package com.kjh.mynote.ui.features.statistics.purchasenote.adapter.paymentmethod

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhHomeCategoryPutchaseStatsItemBinding
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.ui.features.statistics.purchasenote.PaymentMethodStatsItem

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 19..
 * Description:
 */
class PaymentMethodStatsListAdapter(
    private val paymentMethodStatsClickAction: (PaymentMethodStatsUiModel) -> Unit
): ListAdapter<PaymentMethodStatsItem, PaymentMethodStatsListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PaymentMethodStatsListItemViewHolder(
            VhHomeCategoryPutchaseStatsItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), paymentMethodStatsClickAction
        )

    override fun onBindViewHolder(holder: PaymentMethodStatsListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<PaymentMethodStatsItem>() {
                override fun areItemsTheSame(
                    oldItem: PaymentMethodStatsItem,
                    newItem: PaymentMethodStatsItem,
                ): Boolean =
                    oldItem.paymentMethodStatsItem.paymentMethodId ==
                            newItem.paymentMethodStatsItem.paymentMethodId

                override fun areContentsTheSame(
                    oldItem: PaymentMethodStatsItem,
                    newItem: PaymentMethodStatsItem,
                ): Boolean = oldItem == newItem
            }
    }

}
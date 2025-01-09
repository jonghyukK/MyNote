package com.kjh.mynote.ui.features.purchase.search.adapter

import android.graphics.Typeface
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchCategoryListItemBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 6..
 * Description:
 */
class PurchaseNoteSearchPaymentMethodListItemViewHolder(
    private val binding: VhPurchaseNoteSearchCategoryListItemBinding,
    private val paymentMethodFilterClickAction: (Filters.PaymentMethod) -> Unit
): BaseViewHolder<Filters.PaymentMethod>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> paymentMethodFilterClickAction(item) }
        }
    }

    override fun bind(item: Filters.PaymentMethod) {
        super.bind(item)

        with (binding) {
            tvFilterName.text = item.paymentMethod.paymentMethodName

            if (item.isApplied()) {
                tvFilterName.setTypeface(null, Typeface.BOLD)
                tvFilterName.setTextColorRes(appliedTextColor)
            } else {
                tvFilterName.setTypeface(null, Typeface.NORMAL)
                tvFilterName.setTextColorRes(normalTextColor)
            }
            ivImage.isVisible = item.isApplied()
        }
    }

    companion object {
        private val appliedTextColor = R.color.colorPrimary
        private val normalTextColor = R.color.black_600
    }
}
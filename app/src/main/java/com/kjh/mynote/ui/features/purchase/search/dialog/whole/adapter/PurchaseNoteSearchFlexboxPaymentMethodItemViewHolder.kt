package com.kjh.mynote.ui.features.purchase.search.dialog.whole.adapter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 9..
 * Description:
 */

class PurchaseNoteSearchFlexboxPaymentMethodItemViewHolder(
    private val binding: VhFilterCategoryItemBinding,
    private val paymentMethodItemClickAction: (Filters.PaymentMethod) -> Unit
): BaseViewHolder<Filters.PaymentMethod>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> paymentMethodItemClickAction(item) }
        }
    }

    override fun bind(item: Filters.PaymentMethod) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.paymentMethod.paymentMethodName

            if (item.isApplied()) {
                root.background = context.getDrawableCompat(R.drawable.shape_s_color_primary_c_20)
                tvCategoryName.setTextColorRes(R.color.white)
            } else {
                root.background = context.getDrawableCompat(R.drawable.shape_s_white_c_20_l_color_primary)
                tvCategoryName.setTextColorRes(R.color.colorPrimary)
            }
        }
    }
}
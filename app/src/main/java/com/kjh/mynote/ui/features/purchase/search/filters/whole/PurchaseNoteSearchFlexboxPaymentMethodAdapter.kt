package com.kjh.mynote.ui.features.purchase.search.filters.whole

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.purchase.search.Filters
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 6..
 * Description:
 */
class PurchaseNoteSearchFlexboxPaymentMethodAdapter(
    private val paymentMethodItemClickAction: (Filters.PaymentMethod) -> Unit
): ListAdapter<Filters.PaymentMethod, PurchaseNoteSearchFlexboxPaymentMethodItemViewHolder>(UI_MODEL_COMPARATOR) {

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
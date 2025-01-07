package com.kjh.mynote.ui.features.purchase.filter.adapter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhSelectableCategoryListItemBinding
import com.kjh.mynote.model.Filters
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */

class PurchaseNoteHomeFilterListItemViewHolder(
    private val binding: VhSelectableCategoryListItemBinding,
    private val filterItemClickAction: (Filters) -> Unit
): BaseViewHolder<Filters>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> filterItemClickAction(item) }
        }
    }

    override fun bind(item: Filters) {
        super.bind(item)

        when (item) {
            is Filters.Category -> {
                setItemName(item.categoryItem.categoryName)
                updateCheckBox(item.isSelected)
            }
            is Filters.PaymentMethod -> {
                setItemName(item.paymentMethod.paymentMethodName)
                updateCheckBox(item.isSelected)
            }
            else -> {}
        }
    }

    private fun setItemName(name: String) = with (binding.tvTitle) {
        text = name
    }

    private fun updateCheckBox(checked: Boolean) = with (binding.ivCheckbox) {
        if (checked) {
            setImageDrawable(context.getDrawableCompat(R.drawable.ic_selected_checkbox))
            setTint(R.color.colorPrimary)
        } else {
            setImageDrawable(context.getDrawableCompat(R.drawable.ic_unselected_checkbox))
            setTint(R.color.black_200)
        }
    }
}
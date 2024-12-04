package com.kjh.mynote.ui.features.place.make.adapter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhAddTempPurchaseNoteItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.make.TempPurchaseNoteItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */
class TempPurchaseNoteItemViewHolder(
    private val binding: VhAddTempPurchaseNoteItemBinding,
    private val tempPurchaseNoteItemClickAction: (TempPurchaseNoteItem) -> Unit,
    private val removePurchaseNoteBtnClickAction: (TempPurchaseNoteItem) -> Unit
): BaseViewHolder<TempPurchaseNoteItem>(binding.root) {

    init {
        binding.clPurchaseInfo.onThrottleClick {
            bindItem?.let { item -> tempPurchaseNoteItemClickAction(item) }
        }

        binding.ivRemove.onThrottleClick {
            bindItem?.let { item -> removePurchaseNoteBtnClickAction(item) }
        }
    }

    override fun bind(item: TempPurchaseNoteItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryItem?.categoryName ?: "카레고리 없음"
            tvPurchaseName.text = item.purchaseName
            tvPrice.text = context.getString(R.string.format_won, item.purchasePrice.toComma())
        }
    }
}
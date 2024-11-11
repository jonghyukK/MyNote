package com.kjh.mynote.ui.features.purchase.adapter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteHomeListItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class PurchaseNoteHomeListItemViewHolder(
    private val binding: VhPurchaseNoteHomeListItemBinding
): BaseViewHolder<PurchaseNoteUiModel>(binding.root) {

    override fun bind(item: PurchaseNoteUiModel) {
        super.bind(item)

        with (binding) {
            tvCategory.text = item.category?.categoryName ?: "카테고리 없음"
            tvPurchaseName.text = item.purchaseName
            tvPrice.text = context.getString(R.string.format_won, item.purchasePrice.toComma())
        }
    }
}
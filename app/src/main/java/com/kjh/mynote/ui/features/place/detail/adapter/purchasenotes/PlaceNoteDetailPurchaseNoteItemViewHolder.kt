package com.kjh.mynote.ui.features.place.detail.adapter.purchasenotes

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 6..
 * Description:
 */


class PlaceNoteDetailPurchaseNoteItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultItemBinding,
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
): BaseViewHolder<PurchaseNoteUiModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> purchaseNoteItemClickAction(item) }
        }
    }

    override fun bind(item: PurchaseNoteUiModel) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.category?.categoryName ?: "카레고리 없음"
            tvPurchaseName.text = item.purchaseName
            tvPrice.text = context.getString(R.string.format_won, item.purchasePrice.toComma())
        }
    }
}
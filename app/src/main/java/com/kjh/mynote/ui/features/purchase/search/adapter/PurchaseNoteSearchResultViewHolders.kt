package com.kjh.mynote.ui.features.purchase.search.adapter

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultDateItemBinding
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchResultItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */

/**
 * 구매노트 검색 결과 날짜 ViewHolder.
 *
 * @property binding
 */
class PurchaseNoteSearchDateItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultDateItemBinding
): BaseViewHolder<PurchaseNoteSearchResultItem.DateItem>(binding.root) {

    override fun bind(item: PurchaseNoteSearchResultItem.DateItem) {
        super.bind(item)

        binding.tvDate.text = item.date.toStringWithPattern("yyyy년 M월 d일 (E)")
    }
}

/**
 * 구매노트 검색 결과 항목 ViewHolder.
 *
 * @property binding
 * @property onClickAction
 */
class PurchaseNoteSearchResultItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultItemBinding,
    private val onClickAction: (PurchaseNoteUiModel) -> Unit
): BaseViewHolder<PurchaseNoteSearchResultItem.ResultItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onClickAction.invoke(item.purchaseNoteItem) }
        }
    }

    override fun bind(item: PurchaseNoteSearchResultItem.ResultItem) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.purchaseNoteItem.category?.categoryName ?: "카레고리 없음"
            tvPurchaseName.text = item.purchaseNoteItem.purchaseName
            tvPrice.text = context.getString(R.string.format_won, item.purchaseNoteItem.purchasePrice.toComma())
        }
    }
}
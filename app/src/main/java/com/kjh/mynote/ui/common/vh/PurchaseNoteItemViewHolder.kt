package com.kjh.mynote.ui.common.vh

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPurchaseNoteSearchResultItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

/**
 * 구매노트 목록 Item ViewHolder.
 *
 * @property binding
 * @property onClickAction
 */
class PurchaseNoteItemViewHolder(
    private val binding: VhPurchaseNoteSearchResultItemBinding,
    private val onClickAction: (PurchaseNoteUiModel) -> Unit
): BaseViewHolder<PurchaseNoteUiModel>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> onClickAction.invoke(item) }
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
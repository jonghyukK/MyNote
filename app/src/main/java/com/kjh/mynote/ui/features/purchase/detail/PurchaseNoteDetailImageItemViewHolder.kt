package com.kjh.mynote.ui.features.purchase.detail

import com.kjh.mynote.databinding.VhPurchaseNoteDetailImageItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class PurchaseNoteDetailImageItemViewHolder(
    private val binding: VhPurchaseNoteDetailImageItemBinding,
    private val imageClickAction: (String) -> Unit
): BaseViewHolder<String>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> imageClickAction.invoke(item) }
        }
    }

    override fun bind(item: String) {
        super.bind(item)

        binding.ivImage.loadImage(item)
    }
}
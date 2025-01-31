package com.kjh.mynote.ui.features.purchase.makeedit.adapter

import com.kjh.mynote.databinding.VhRecentRegisteredPurchaseNameItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 18..
 * Description:
 */
class RecentRegisteredPurchaseNameItemViewHolder(
    private val binding: VhRecentRegisteredPurchaseNameItemBinding,
    private val recentPurchaseNameClickAction: (String) -> Unit
): BaseViewHolder<String>(binding.root) {

    init {
        itemView.addClickAnimation()
        itemView.onThrottleClick {
            bindItem?.let { item -> recentPurchaseNameClickAction(item) }
        }
    }

    override fun bind(item: String) {
        super.bind(item)

        binding.tvName.text = item
    }
}
package com.kjh.mynote.ui.features.purchase.statistics.adapter.contents

import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.purchase.statistics.SeeAllEvent
import com.kjh.mynote.ui.features.purchase.statistics.StatsContentsItem
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 13..
 * Description:
 */

class SeeAllItemViewHolder(
    private val binding: VhHomePlaceNoteWeekViewInnerMoreItemBinding,
    private val showAllClickAction: (SeeAllEvent) -> Unit,
) : BaseViewHolder<StatsContentsItem.SeeAllItem>(binding.root) {

    init {
        binding.tvMore.onThrottleClick {
            bindItem?.let { item -> showAllClickAction(item.eventType) }
        }
    }

    override fun bind(item: StatsContentsItem.SeeAllItem) {
        super.bind(item)
        binding.tvMore.text = context.getString(R.string.do_show_all)
    }
}
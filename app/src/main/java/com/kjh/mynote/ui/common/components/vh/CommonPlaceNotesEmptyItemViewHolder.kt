package com.kjh.mynote.ui.common.components.vh

import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 3..
 * Description:
 */

class CommonPlaceNotesEmptyItemViewHolder(
    private val binding: LayoutEmptyMyPlacesBinding,
    private val makeNoteClickAction: () -> Unit
): BaseViewHolder<Unit>(binding.root) {

    init {
        binding.btnMakePlace.onThrottleClick {
            makeNoteClickAction.invoke()
        }
    }
}
package com.kjh.mynote.ui.features.place.detail

import com.kjh.mynote.databinding.VhPlaceNoteDetailPagerImageItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailPagerItemViewHolder(
    private val binding: VhPlaceNoteDetailPagerImageItemBinding,
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
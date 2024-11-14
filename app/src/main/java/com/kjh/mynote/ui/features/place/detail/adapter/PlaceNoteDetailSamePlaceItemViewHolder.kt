package com.kjh.mynote.ui.features.place.detail.adapter

import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlaceItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toStringWithFormat

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 24..
 * Description:
 */
class PlaceNoteDetailSamePlaceItemViewHolder(
    private val binding: VhPlaceNoteDetailSamePlaceItemBinding,
    private val samePlaceItemClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteUiModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> samePlaceItemClickAction.invoke(item) }
        }
    }

    override fun bind(item: PlaceNoteUiModel) {
        super.bind(item)

        with (binding) {
            ivImage.loadImage(item.placeImages[0])
            tvVisitDate.text = item.visitDate.toStringWithFormat("yyyy년 M월 d일 (E)")
        }
    }
}
package com.kjh.mynote.ui.features.place.search.result.adapter

import com.kjh.mynote.databinding.VhPlaceNoteSearchResultInnerItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class PlaceNoteSearchResultInnerItemViewHolder(
    private val binding: VhPlaceNoteSearchResultInnerItemBinding,
    private val onClickAction: (PlaceNoteUiModel) -> Unit,
): BaseViewHolder<PlaceNoteUiModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onClickAction.invoke(item) }
        }
    }

    override fun bind(item: PlaceNoteUiModel) {
        super.bind(item)

        with (binding) {
            ivImage.loadImage(item.placeImages[0])
            tvPlaceName.text = item.placeInfo.placeName
            tvPlaceRegion.text = item.placeInfo.placeRegion
        }
    }
}
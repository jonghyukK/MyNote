package com.kjh.mynote.ui.features.place.calendar.list.adapter

import com.kjh.mynote.databinding.VhPlaceNoteInCalendarHeaderBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypePlaceItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.calendar.ListTypeCalendarPlaceNoteUI
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

class PlaceNoteListTypeInnerHeaderItemViewHolder(
    private val binding: VhPlaceNoteInCalendarHeaderBinding
): BaseViewHolder<ListTypeCalendarPlaceNoteUI.HeaderItem>(binding.root) {

    override fun bind(item: ListTypeCalendarPlaceNoteUI.HeaderItem) {
        super.bind(item)

        binding.tvDay.text = item.localDate.toStringWithPattern("d일 (E)")
    }
}

class PlaceNoteListTypeInnerContentsItemViewHolder(
    private val binding: VhPlaceNoteInCalendarListTypePlaceItemBinding,
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<ListTypeCalendarPlaceNoteUI.PlaceNoteItem>(binding.root) {

    init {
        binding.cvCardContainer.onThrottleClick {
            bindItem?.let { item -> placeItemClickAction.invoke(item.item) }
        }
    }

    override fun bind(item: ListTypeCalendarPlaceNoteUI.PlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage.loadImage(item.item.placeImages[0])
            tvPlaceName.text = item.item.placeName
            tvPlaceArea.text = item.item.placeRegion
        }
    }
}



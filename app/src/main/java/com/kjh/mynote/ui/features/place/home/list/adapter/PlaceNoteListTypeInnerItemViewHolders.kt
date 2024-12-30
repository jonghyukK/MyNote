package com.kjh.mynote.ui.features.place.home.list.adapter

import com.kjh.mynote.databinding.VhPlaceNoteInCalendarHeaderBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypePlaceItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.home.list.PlaceNoteListTypeUiItem
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

/**
 * 장소노트 ListType 화면 - 장소노트 Date Item ViewHolder.
 *
 * @property binding
 */
class PlaceNoteListTypeInnerHeaderItemViewHolder(
    private val binding: VhPlaceNoteInCalendarHeaderBinding
): BaseViewHolder<PlaceNoteListTypeUiItem.DateItem>(binding.root) {

    override fun bind(item: PlaceNoteListTypeUiItem.DateItem) {
        super.bind(item)

        binding.tvDay.text = item.localDate.toStringWithPattern("d일 (E)")
    }
}

/**
 * 장소노트 ListType 화면 - 장소노트 Item ViewHolder.
 *
 * @property binding
 */
class PlaceNoteListTypeInnerContentsItemViewHolder(
    private val binding: VhPlaceNoteInCalendarListTypePlaceItemBinding,
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteListTypeUiItem.PlaceNoteItem>(binding.root) {

    init {
        binding.cvCardContainer.onThrottleClick {
            bindItem?.let { item -> placeItemClickAction.invoke(item.item) }
        }
    }

    override fun bind(item: PlaceNoteListTypeUiItem.PlaceNoteItem) {
        super.bind(item)

        with (binding) {
            ivImage.loadImage(item.item.placeImages[0])
            tvPlaceName.text = item.item.placeInfo.placeName
            tvPlaceArea.text = item.item.placeInfo.placeRegion
        }
    }
}



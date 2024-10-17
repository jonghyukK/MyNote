package com.kjh.mynote.ui.features.place.calendar

import com.bumptech.glide.Glide
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 17..
 * Description:
 */

class CalendarWithPlacesListItemViewHolder(
    private val binding: VhPlaceNoteInCalendarItemBinding,
    private val placeClickAction: (PlaceNoteModel) -> Unit
): BaseViewHolder<PlaceNoteModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeClickAction.invoke(item) }
        }
    }

    override fun bind(item: PlaceNoteModel) {
        super.bind(item)

        Glide.with(context)
            .load(item.placeImages[0])
            .into(binding.ivPlaceFirstImage)

        binding.tvPlaceName.text = item.placeName
        binding.tvPlaceAddress.text = item.placeAddress
    }
}
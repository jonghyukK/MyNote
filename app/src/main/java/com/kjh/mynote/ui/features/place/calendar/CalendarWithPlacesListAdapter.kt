package com.kjh.mynote.ui.features.place.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarFourPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOnePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOverPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarThreePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarTwoPictureItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */
class CalendarPlaceListAdapter(
    private val placeClickAction: (PlaceNoteModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit
): ListAdapter<CalendarPlaceNoteUiState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

        override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        R.layout.vh_place_note_in_calendar_one_picture_item -> {
            CalendarPlaceNoteOnePictureItemViewHolder(
                VhPlaceNoteInCalendarOnePictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        R.layout.vh_place_note_in_calendar_two_picture_item -> {
            CalendarPlaceNoteTwoPictureItemViewHolder(
                VhPlaceNoteInCalendarTwoPictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        R.layout.vh_place_note_in_calendar_three_picture_item -> {
            CalendarPlaceNoteThreePictureItemViewHolder(
                VhPlaceNoteInCalendarThreePictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        R.layout.vh_place_note_in_calendar_four_picture_item -> {
            CalendarPlaceNoteFourPictureItemViewHolder(
                VhPlaceNoteInCalendarFourPictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        R.layout.vh_place_note_in_calendar_over_picture_item -> {
            CalendarPlaceNoteOverPictureItemViewHolder(
                VhPlaceNoteInCalendarOverPictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }
        else -> throw Exception("wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CalendarPlaceNoteUiState.OnePicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteOnePictureItemViewHolder).bind(item)

            is CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteTwoPictureItemViewHolder).bind(item)

            is CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteThreePictureItemViewHolder).bind(item)

            is CalendarPlaceNoteUiState.FourPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteFourPictureItemViewHolder).bind(item)

            is CalendarPlaceNoteUiState.OverPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteOverPictureItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CalendarPlaceNoteUiState.OnePicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_one_picture_item

        is CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_two_picture_item

        is CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_three_picture_item

        is CalendarPlaceNoteUiState.FourPicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_four_picture_item

        is CalendarPlaceNoteUiState.OverPicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_over_picture_item
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<CalendarPlaceNoteUiState>() {
            override fun areItemsTheSame(
                oldItem: CalendarPlaceNoteUiState,
                newItem: CalendarPlaceNoteUiState
            ): Boolean =
                if (oldItem is CalendarPlaceNoteUiState.OnePicturePlaceNoteItem
                    && newItem is CalendarPlaceNoteUiState.OnePicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem
                    && newItem is CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem
                    && newItem is CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is CalendarPlaceNoteUiState.FourPicturePlaceNoteItem
                    && newItem is CalendarPlaceNoteUiState.FourPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is CalendarPlaceNoteUiState.OverPicturePlaceNoteItem
                    && newItem is CalendarPlaceNoteUiState.OverPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else {
                    false
                }

            override fun areContentsTheSame(
                oldItem: CalendarPlaceNoteUiState,
                newItem: CalendarPlaceNoteUiState
            ): Boolean = oldItem == newItem
        }
    }
}
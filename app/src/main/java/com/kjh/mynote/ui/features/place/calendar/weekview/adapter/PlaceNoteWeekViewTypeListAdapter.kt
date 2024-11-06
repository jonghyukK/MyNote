package com.kjh.mynote.ui.features.place.calendar.weekview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarFourPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOnePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOverPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarThreePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarTwoPictureItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.common.components.vh.CommonPlaceNotesEmptyItemViewHolder
import com.kjh.mynote.ui.features.place.calendar.WeekViewTypeCalendarPlaceNoteUI

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */
class PlaceNoteWeekViewTypeListAdapter(
    private val placeClickAction: (PlaceNoteUiModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit,
    private val makeNoteClickAction: () -> Unit
): ListAdapter<WeekViewTypeCalendarPlaceNoteUI, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        R.layout.layout_empty_my_places -> {
            CommonPlaceNotesEmptyItemViewHolder(
                LayoutEmptyMyPlacesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), makeNoteClickAction
            )
        }

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
            is WeekViewTypeCalendarPlaceNoteUI.EmptyItem ->
                (holder as CommonPlaceNotesEmptyItemViewHolder).bind(Unit)

            is WeekViewTypeCalendarPlaceNoteUI.OnePicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteOnePictureItemViewHolder).bind(item)

            is WeekViewTypeCalendarPlaceNoteUI.TwoPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteTwoPictureItemViewHolder).bind(item)

            is WeekViewTypeCalendarPlaceNoteUI.ThreePicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteThreePictureItemViewHolder).bind(item)

            is WeekViewTypeCalendarPlaceNoteUI.FourPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteFourPictureItemViewHolder).bind(item)

            is WeekViewTypeCalendarPlaceNoteUI.OverPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteOverPictureItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is WeekViewTypeCalendarPlaceNoteUI.EmptyItem ->
            R.layout.layout_empty_my_places

        is WeekViewTypeCalendarPlaceNoteUI.OnePicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_one_picture_item

        is WeekViewTypeCalendarPlaceNoteUI.TwoPicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_two_picture_item

        is WeekViewTypeCalendarPlaceNoteUI.ThreePicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_three_picture_item

        is WeekViewTypeCalendarPlaceNoteUI.FourPicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_four_picture_item

        is WeekViewTypeCalendarPlaceNoteUI.OverPicturePlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_over_picture_item
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<WeekViewTypeCalendarPlaceNoteUI>() {
            override fun areItemsTheSame(
                oldItem: WeekViewTypeCalendarPlaceNoteUI,
                newItem: WeekViewTypeCalendarPlaceNoteUI
            ): Boolean =
                if (oldItem is WeekViewTypeCalendarPlaceNoteUI.OnePicturePlaceNoteItem
                    && newItem is WeekViewTypeCalendarPlaceNoteUI.OnePicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is WeekViewTypeCalendarPlaceNoteUI.TwoPicturePlaceNoteItem
                    && newItem is WeekViewTypeCalendarPlaceNoteUI.TwoPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is WeekViewTypeCalendarPlaceNoteUI.ThreePicturePlaceNoteItem
                    && newItem is WeekViewTypeCalendarPlaceNoteUI.ThreePicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is WeekViewTypeCalendarPlaceNoteUI.FourPicturePlaceNoteItem
                    && newItem is WeekViewTypeCalendarPlaceNoteUI.FourPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is WeekViewTypeCalendarPlaceNoteUI.OverPicturePlaceNoteItem
                    && newItem is WeekViewTypeCalendarPlaceNoteUI.OverPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else {
                    false
                }

            override fun areContentsTheSame(
                oldItem: WeekViewTypeCalendarPlaceNoteUI,
                newItem: WeekViewTypeCalendarPlaceNoteUI
            ): Boolean = oldItem == newItem
        }
    }
}
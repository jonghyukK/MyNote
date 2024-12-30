package com.kjh.mynote.ui.features.place.home.weekview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarFourPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOnePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarOverPictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarThreePictureItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarTwoPictureItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.common.components.vh.CommonPlaceNotesEmptyItemViewHolder
import com.kjh.mynote.ui.features.place.home.weekview.PlaceNoteWeekViewUiItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */
class PlaceNoteWeekViewTypeListAdapter(
    private val placeClickAction: (PlaceNoteUiModel) -> Unit,
    private val imageClickAction: (List<String>, String) -> Unit,
    private val makeNoteClickAction: () -> Unit
): ListAdapter<PlaceNoteWeekViewUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (viewType) {
        VIEW_TYPE_EMPTY -> {
            CommonPlaceNotesEmptyItemViewHolder(
                LayoutEmptyMyPlacesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), makeNoteClickAction
            )
        }

        VIEW_TYPE_ONE_PICTURE -> {
            CalendarPlaceNoteOnePictureItemViewHolder(
                VhPlaceNoteInCalendarOnePictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        VIEW_TYPE_TWO_PICTURE -> {
            CalendarPlaceNoteTwoPictureItemViewHolder(
                VhPlaceNoteInCalendarTwoPictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        VIEW_TYPE_THREE_PICTURE -> {
            CalendarPlaceNoteThreePictureItemViewHolder(
                VhPlaceNoteInCalendarThreePictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        VIEW_TYPE_FOUR_PICTURE -> {
            CalendarPlaceNoteFourPictureItemViewHolder(
                VhPlaceNoteInCalendarFourPictureItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeClickAction, imageClickAction
            )
        }

        VIEW_TYPE_OVER_PICTURE -> {
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
            is PlaceNoteWeekViewUiItem.EmptyItem ->
                (holder as CommonPlaceNotesEmptyItemViewHolder).bind(Unit)

            is PlaceNoteWeekViewUiItem.OnePicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteOnePictureItemViewHolder).bind(item)

            is PlaceNoteWeekViewUiItem.TwoPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteTwoPictureItemViewHolder).bind(item)

            is PlaceNoteWeekViewUiItem.ThreePicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteThreePictureItemViewHolder).bind(item)

            is PlaceNoteWeekViewUiItem.FourPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteFourPictureItemViewHolder).bind(item)

            is PlaceNoteWeekViewUiItem.OverPicturePlaceNoteItem ->
                (holder as CalendarPlaceNoteOverPictureItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PlaceNoteWeekViewUiItem.EmptyItem -> VIEW_TYPE_EMPTY
        is PlaceNoteWeekViewUiItem.OnePicturePlaceNoteItem -> VIEW_TYPE_ONE_PICTURE
        is PlaceNoteWeekViewUiItem.TwoPicturePlaceNoteItem -> VIEW_TYPE_TWO_PICTURE
        is PlaceNoteWeekViewUiItem.ThreePicturePlaceNoteItem -> VIEW_TYPE_THREE_PICTURE
        is PlaceNoteWeekViewUiItem.FourPicturePlaceNoteItem -> VIEW_TYPE_FOUR_PICTURE
        is PlaceNoteWeekViewUiItem.OverPicturePlaceNoteItem -> VIEW_TYPE_OVER_PICTURE
    }

    companion object {
        private const val VIEW_TYPE_ONE_PICTURE = 1
        private const val VIEW_TYPE_TWO_PICTURE = 2
        private const val VIEW_TYPE_THREE_PICTURE = 3
        private const val VIEW_TYPE_FOUR_PICTURE = 4
        private const val VIEW_TYPE_OVER_PICTURE = 5
        private const val VIEW_TYPE_EMPTY = 6

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteWeekViewUiItem>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteWeekViewUiItem,
                newItem: PlaceNoteWeekViewUiItem
            ): Boolean =
                if (oldItem is PlaceNoteWeekViewUiItem.OnePicturePlaceNoteItem
                    && newItem is PlaceNoteWeekViewUiItem.OnePicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is PlaceNoteWeekViewUiItem.TwoPicturePlaceNoteItem
                    && newItem is PlaceNoteWeekViewUiItem.TwoPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is PlaceNoteWeekViewUiItem.ThreePicturePlaceNoteItem
                    && newItem is PlaceNoteWeekViewUiItem.ThreePicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is PlaceNoteWeekViewUiItem.FourPicturePlaceNoteItem
                    && newItem is PlaceNoteWeekViewUiItem.FourPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else if (oldItem is PlaceNoteWeekViewUiItem.OverPicturePlaceNoteItem
                    && newItem is PlaceNoteWeekViewUiItem.OverPicturePlaceNoteItem
                ) {
                    oldItem.item.id == newItem.item.id
                } else {
                    false
                }

            override fun areContentsTheSame(
                oldItem: PlaceNoteWeekViewUiItem,
                newItem: PlaceNoteWeekViewUiItem
            ): Boolean = oldItem == newItem
        }
    }
}
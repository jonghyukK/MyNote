package com.kjh.mynote.ui.features.place.calendar.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarHeaderBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypePlaceItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.common.components.vh.CommonPlaceNotesEmptyItemViewHolder
import com.kjh.mynote.ui.features.place.calendar.ListTypeCalendarPlaceNoteUI

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

class PlaceNoteListTypeInnerAdapter(
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit,
    private val makeNoteClickAction: () -> Unit
): ListAdapter<ListTypeCalendarPlaceNoteUI, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.vh_place_note_in_calendar_header -> {
            PlaceNoteListTypeInnerHeaderItemViewHolder(
                VhPlaceNoteInCalendarHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        R.layout.vh_place_note_in_calendar_list_type_place_item -> {
            PlaceNoteListTypeInnerContentsItemViewHolder(
                VhPlaceNoteInCalendarListTypePlaceItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeItemClickAction
            )
        }
        R.layout.layout_empty_my_places -> {
            CommonPlaceNotesEmptyItemViewHolder(
                LayoutEmptyMyPlacesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), makeNoteClickAction
            )
        }
        else -> throw Exception("Wrong ViewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListTypeCalendarPlaceNoteUI.HeaderItem -> {
                (holder as PlaceNoteListTypeInnerHeaderItemViewHolder).bind(item)
            }
            is ListTypeCalendarPlaceNoteUI.PlaceNoteItem -> {
                (holder as PlaceNoteListTypeInnerContentsItemViewHolder).bind(item)
            }
            is ListTypeCalendarPlaceNoteUI.EmptyItem -> {
                (holder as CommonPlaceNotesEmptyItemViewHolder).bind(Unit)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ListTypeCalendarPlaceNoteUI.HeaderItem ->
            R.layout.vh_place_note_in_calendar_header

        is ListTypeCalendarPlaceNoteUI.PlaceNoteItem ->
            R.layout.vh_place_note_in_calendar_list_type_place_item

        is ListTypeCalendarPlaceNoteUI.EmptyItem ->
            R.layout.layout_empty_my_places
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<ListTypeCalendarPlaceNoteUI>() {
            override fun areItemsTheSame(
                oldItem: ListTypeCalendarPlaceNoteUI,
                newItem: ListTypeCalendarPlaceNoteUI
            ): Boolean = if (oldItem is ListTypeCalendarPlaceNoteUI.HeaderItem
                && newItem is ListTypeCalendarPlaceNoteUI.HeaderItem) {
                oldItem.localDate == newItem.localDate
            } else if (oldItem is ListTypeCalendarPlaceNoteUI.PlaceNoteItem
                && newItem is ListTypeCalendarPlaceNoteUI.PlaceNoteItem) {
                oldItem.item.id == newItem.item.id
            } else if (oldItem is ListTypeCalendarPlaceNoteUI.EmptyItem
                && newItem is ListTypeCalendarPlaceNoteUI.EmptyItem) {
              true
            } else {
                false
            }

            override fun areContentsTheSame(
                oldItem: ListTypeCalendarPlaceNoteUI,
                newItem: ListTypeCalendarPlaceNoteUI
            ): Boolean = oldItem == newItem
        }
    }
}

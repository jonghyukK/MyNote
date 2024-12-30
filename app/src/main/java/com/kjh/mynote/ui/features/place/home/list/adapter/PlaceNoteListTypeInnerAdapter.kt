package com.kjh.mynote.ui.features.place.home.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarHeaderBinding
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypePlaceItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.common.components.vh.CommonPlaceNotesEmptyItemViewHolder
import com.kjh.mynote.ui.features.place.home.list.PlaceNoteListTypeUiItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

class PlaceNoteListTypeInnerAdapter(
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit,
    private val makeNoteClickAction: () -> Unit
): ListAdapter<PlaceNoteListTypeUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_DATE -> {
            PlaceNoteListTypeInnerHeaderItemViewHolder(
                VhPlaceNoteInCalendarHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        VIEW_TYPE_PLACE_NOTE -> {
            PlaceNoteListTypeInnerContentsItemViewHolder(
                VhPlaceNoteInCalendarListTypePlaceItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeItemClickAction
            )
        }
        VIEW_TYPE_EMPTY -> {
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
            is PlaceNoteListTypeUiItem.DateItem -> {
                (holder as PlaceNoteListTypeInnerHeaderItemViewHolder).bind(item)
            }
            is PlaceNoteListTypeUiItem.PlaceNoteItem -> {
                (holder as PlaceNoteListTypeInnerContentsItemViewHolder).bind(item)
            }
            is PlaceNoteListTypeUiItem.EmptyItem -> {
                (holder as CommonPlaceNotesEmptyItemViewHolder).bind(Unit)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PlaceNoteListTypeUiItem.DateItem -> VIEW_TYPE_DATE
        is PlaceNoteListTypeUiItem.PlaceNoteItem -> VIEW_TYPE_PLACE_NOTE
        is PlaceNoteListTypeUiItem.EmptyItem -> VIEW_TYPE_EMPTY
    }

    companion object {
        private const val VIEW_TYPE_DATE = 1
        private const val VIEW_TYPE_PLACE_NOTE = 2
        private const val VIEW_TYPE_EMPTY = 3

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteListTypeUiItem>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteListTypeUiItem,
                newItem: PlaceNoteListTypeUiItem
            ): Boolean = when {
                oldItem is PlaceNoteListTypeUiItem.DateItem
                        && newItem is PlaceNoteListTypeUiItem.DateItem -> {
                            oldItem.localDate == newItem.localDate
                        }
                oldItem is PlaceNoteListTypeUiItem.PlaceNoteItem
                        && newItem is PlaceNoteListTypeUiItem.PlaceNoteItem -> {
                            oldItem.item.id == newItem.item.id
                        }
                oldItem is PlaceNoteListTypeUiItem.EmptyItem
                        && newItem is PlaceNoteListTypeUiItem.EmptyItem -> {
                            true
                        }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: PlaceNoteListTypeUiItem,
                newItem: PlaceNoteListTypeUiItem
            ): Boolean = oldItem == newItem
        }
    }
}

package com.kjh.mynote.ui.features.home.adapter.weeklyplacenotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.databinding.VhHomePlaceeNoteWeekViewPlaceNoteItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.home.WeeklyPlaceNoteUiItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */
class HomeWeeklyPlaceNoteListAdapter(
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit,
    private val makePlaceNoteClickAction: () -> Unit,
    private val seeAllPlaceNotesClickAction: () -> Unit
): ListAdapter<WeeklyPlaceNoteUiItem, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        // Empty
        R.layout.layout_empty_my_places -> {
            HomePlaceNoteWeekViewInnerEmptyItemViewHolder(
                LayoutEmptyMyPlacesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), makePlaceNoteClickAction
            )
        }
        // More
        R.layout.vh_home_place_note_week_view_inner_more_item -> {
            HomePlaceNoteWeekViewInnerMoreItemViewHolder(
                VhHomePlaceNoteWeekViewInnerMoreItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), seeAllPlaceNotesClickAction
            )
        }
        // Place Note
        R.layout.vh_home_placee_note_week_view_place_note_item -> {
            HomePlaceNoteWeekViewInnerPlaceNoteItemViewHolder(
                VhHomePlaceeNoteWeekViewPlaceNoteItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), placeNoteClickAction
            )
        }
        else -> throw Exception("wrong ViewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is WeeklyPlaceNoteUiItem.Empty -> 
                (holder as HomePlaceNoteWeekViewInnerEmptyItemViewHolder).bind(Unit)
            is WeeklyPlaceNoteUiItem.More -> 
                (holder as HomePlaceNoteWeekViewInnerMoreItemViewHolder).bind(Unit)
            is WeeklyPlaceNoteUiItem.PlaceNote ->
                (holder as HomePlaceNoteWeekViewInnerPlaceNoteItemViewHolder).bind(item.item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is WeeklyPlaceNoteUiItem.Empty -> R.layout.layout_empty_my_places
        is WeeklyPlaceNoteUiItem.More -> R.layout.vh_home_place_note_week_view_inner_more_item
        is WeeklyPlaceNoteUiItem.PlaceNote -> R.layout.vh_home_placee_note_week_view_place_note_item
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<WeeklyPlaceNoteUiItem>() {
            override fun areItemsTheSame(
                oldItem: WeeklyPlaceNoteUiItem,
                newItem: WeeklyPlaceNoteUiItem
            ): Boolean = when {
                oldItem is WeeklyPlaceNoteUiItem.Empty && newItem is WeeklyPlaceNoteUiItem.Empty -> true
                oldItem is WeeklyPlaceNoteUiItem.More && newItem is WeeklyPlaceNoteUiItem.More -> true
                oldItem is WeeklyPlaceNoteUiItem.PlaceNote && newItem is WeeklyPlaceNoteUiItem.PlaceNote -> {
                    oldItem.item.id == newItem.item.id
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: WeeklyPlaceNoteUiItem,
                newItem: WeeklyPlaceNoteUiItem
            ): Boolean = oldItem == newItem
        }
    }
}


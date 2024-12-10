package com.kjh.mynote.ui.features.home.weekview

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
import com.kjh.mynote.ui.features.home.HomePlaceNoteUiState

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */
class HomePlaceNoteWeekViewInnerAdapter(
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit,
    private val makePlaceNoteClickAction: () -> Unit
): ListAdapter<HomePlaceNoteUiState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {
    
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
                )
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
            is HomePlaceNoteUiState.Empty -> 
                (holder as HomePlaceNoteWeekViewInnerEmptyItemViewHolder).bind(Unit)
            is HomePlaceNoteUiState.More -> 
                (holder as HomePlaceNoteWeekViewInnerMoreItemViewHolder).bind(Unit)
            is HomePlaceNoteUiState.PlaceNote ->
                (holder as HomePlaceNoteWeekViewInnerPlaceNoteItemViewHolder).bind(item.item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomePlaceNoteUiState.Empty -> R.layout.layout_empty_my_places
        is HomePlaceNoteUiState.More -> R.layout.vh_home_place_note_week_view_inner_more_item
        is HomePlaceNoteUiState.PlaceNote -> R.layout.vh_home_placee_note_week_view_place_note_item
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<HomePlaceNoteUiState>() {
            override fun areItemsTheSame(
                oldItem: HomePlaceNoteUiState,
                newItem: HomePlaceNoteUiState
            ): Boolean = when {
                oldItem is HomePlaceNoteUiState.Empty && newItem is HomePlaceNoteUiState.Empty -> true
                oldItem is HomePlaceNoteUiState.More && newItem is HomePlaceNoteUiState.More -> true
                oldItem is HomePlaceNoteUiState.PlaceNote && newItem is HomePlaceNoteUiState.PlaceNote -> {
                    oldItem.item.id == newItem.item.id
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: HomePlaceNoteUiState,
                newItem: HomePlaceNoteUiState
            ): Boolean = oldItem == newItem
        }
    }
}


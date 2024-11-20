package com.kjh.mynote.ui.features.place.search.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteSearchResultOuterItemBinding
import com.kjh.mynote.model.FilteredSearchPlaceNotesUiModel
import com.kjh.mynote.model.PlaceNoteUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class PlaceNoteSearchResultOuterListAdapter(
    private val placeNoteItemClickAction: (PlaceNoteUiModel) -> Unit
) : ListAdapter<FilteredSearchPlaceNotesUiModel, PlaceNoteSearchResultOuterItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaceNoteSearchResultOuterItemViewHolder(
        VhPlaceNoteSearchResultOuterItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), placeNoteItemClickAction
    )

    override fun onBindViewHolder(
        holder: PlaceNoteSearchResultOuterItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<FilteredSearchPlaceNotesUiModel>() {
                override fun areItemsTheSame(
                    oldItem: FilteredSearchPlaceNotesUiModel,
                    newItem: FilteredSearchPlaceNotesUiModel
                ): Boolean = oldItem.date == newItem.date

                override fun areContentsTheSame(
                    oldItem: FilteredSearchPlaceNotesUiModel,
                    newItem: FilteredSearchPlaceNotesUiModel
                ): Boolean = oldItem == newItem
            }
    }
}
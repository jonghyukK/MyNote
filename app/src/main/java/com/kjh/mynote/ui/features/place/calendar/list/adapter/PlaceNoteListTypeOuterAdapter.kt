package com.kjh.mynote.ui.features.place.calendar.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.place.calendar.list.MonthWithPlaceNotesItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

class PlaceNoteListTypeOuterAdapter(
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit,
    private val makeNoteClickAction: () -> Unit
): ListAdapter<MonthWithPlaceNotesItem, PlaceNoteListTypeOuterItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaceNoteListTypeOuterItemViewHolder(
        VhPlaceNoteInCalendarListTypeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), placeItemClickAction, makeNoteClickAction
    )

    override fun onBindViewHolder(holder: PlaceNoteListTypeOuterItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<MonthWithPlaceNotesItem>() {
            override fun areItemsTheSame(
                oldItem: MonthWithPlaceNotesItem,
                newItem: MonthWithPlaceNotesItem
            ): Boolean = oldItem.monthDate == newItem.monthDate

            override fun areContentsTheSame(
                oldItem: MonthWithPlaceNotesItem,
                newItem: MonthWithPlaceNotesItem
            ): Boolean = oldItem == newItem
        }
    }
}

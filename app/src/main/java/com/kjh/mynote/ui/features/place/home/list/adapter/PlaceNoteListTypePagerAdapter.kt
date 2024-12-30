package com.kjh.mynote.ui.features.place.home.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.place.home.list.MonthWithPlaceNoteUiItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

class PlaceNoteListTypePagerAdapter(
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit,
    private val makeNoteClickAction: () -> Unit
): ListAdapter<MonthWithPlaceNoteUiItem, PlaceNoteListTypePagerItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaceNoteListTypePagerItemViewHolder(
        VhPlaceNoteInCalendarListTypeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), placeItemClickAction, makeNoteClickAction
    )

    override fun onBindViewHolder(holder: PlaceNoteListTypePagerItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<MonthWithPlaceNoteUiItem>() {
            override fun areItemsTheSame(
                oldItem: MonthWithPlaceNoteUiItem,
                newItem: MonthWithPlaceNoteUiItem
            ): Boolean = oldItem.month == newItem.month

            override fun areContentsTheSame(
                oldItem: MonthWithPlaceNoteUiItem,
                newItem: MonthWithPlaceNoteUiItem
            ): Boolean = oldItem == newItem
        }
    }
}

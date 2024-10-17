package com.kjh.mynote.ui.features.place.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.databinding.VhPlaceNoteInCalendarItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 15..
 * Description:
 */
class CalendarPlaceListAdapter(
    private val placeClickAction: (PlaceNoteModel) -> Unit
): ListAdapter<PlaceNoteModel, CalendarWithPlacesListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = CalendarWithPlacesListItemViewHolder(
        VhPlaceNoteInCalendarItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), placeClickAction
    )

    override fun onBindViewHolder(holder: CalendarWithPlacesListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteModel>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteModel,
                newItem: PlaceNoteModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PlaceNoteModel,
                newItem: PlaceNoteModel
            ): Boolean = oldItem == newItem
        }
    }
}
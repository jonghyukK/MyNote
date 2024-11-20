package com.kjh.mynote.ui.features.place.search.autocomplete.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteSearchAutoCompleteItemBinding
import com.kjh.mynote.ui.features.place.search.autocomplete.PlaceNoteSearchAutoCompleteItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */
class PlaceNoteSearchAutoCompleteListAdapter(
    private val autoCompleteItemClickAction: (PlaceNoteSearchAutoCompleteItem) -> Unit
): ListAdapter<PlaceNoteSearchAutoCompleteItem, PlaceNoteSearchAutoCompleteItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PlaceNoteSearchAutoCompleteItemViewHolder(
            VhPlaceNoteSearchAutoCompleteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), autoCompleteItemClickAction
        )

    override fun onBindViewHolder(holder: PlaceNoteSearchAutoCompleteItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteSearchAutoCompleteItem>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteSearchAutoCompleteItem,
                newItem: PlaceNoteSearchAutoCompleteItem
            ): Boolean = oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(
                oldItem: PlaceNoteSearchAutoCompleteItem,
                newItem: PlaceNoteSearchAutoCompleteItem
            ): Boolean = oldItem == newItem
        }
    }
}
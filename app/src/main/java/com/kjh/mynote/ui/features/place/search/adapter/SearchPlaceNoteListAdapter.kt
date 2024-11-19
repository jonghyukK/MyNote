package com.kjh.mynote.ui.features.place.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhSearchPlaceNoteListItemBinding
import com.kjh.mynote.ui.features.place.search.SearchResultItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */
class SearchPlaceNoteListAdapter(
    private val itemClickAction: (SearchResultItem) -> Unit
): ListAdapter<SearchResultItem, SearchPlaceNoteListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SearchPlaceNoteListItemViewHolder(
            VhSearchPlaceNoteListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), itemClickAction
        )

    override fun onBindViewHolder(holder: SearchPlaceNoteListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<SearchResultItem>() {
            override fun areItemsTheSame(
                oldItem: SearchResultItem,
                newItem: SearchResultItem
            ): Boolean = oldItem.item.id == newItem.item.id

            override fun areContentsTheSame(
                oldItem: SearchResultItem,
                newItem: SearchResultItem
            ): Boolean = oldItem == newItem
        }
    }
}
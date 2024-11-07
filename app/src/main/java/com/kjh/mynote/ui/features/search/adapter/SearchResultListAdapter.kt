package com.kjh.mynote.ui.features.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhSearchResultItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.features.search.SearchResultItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */
class SearchResultListAdapter(
    private val itemClickAction: (SearchResultItem) -> Unit
): ListAdapter<SearchResultItem, SearchResultItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SearchResultItemViewHolder(
            VhSearchResultItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), itemClickAction
        )

    override fun onBindViewHolder(holder: SearchResultItemViewHolder, position: Int) {
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
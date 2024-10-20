package com.kjh.mynote.ui.features.place.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailUiListAdapter(
    private val imageViewerClickAction: (List<String>, String) -> Unit
) : ListAdapter<PlaceNoteDetailUi, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.vh_place_note_detail_item -> {
            PlaceNoteDetailItemViewHolder(
                VhPlaceNoteDetailItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), imageViewerClickAction
            )
        }
        else -> throw Exception("Wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is PlaceNoteDetailUi.PlaceNoteDetailItem ->
                (holder as PlaceNoteDetailItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PlaceNoteDetailUi.PlaceNoteDetailItem -> R.layout.vh_place_note_detail_item
        else -> -1
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteDetailUi>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteDetailUi,
                newItem: PlaceNoteDetailUi
            ): Boolean =
                if (oldItem is PlaceNoteDetailUi.PlaceNoteDetailItem && newItem is PlaceNoteDetailUi.PlaceNoteDetailItem) {
                    oldItem.placeNoteItem.id == newItem.placeNoteItem.id
                } else {
                    false
                }

            override fun areContentsTheSame(
                oldItem: PlaceNoteDetailUi,
                newItem: PlaceNoteDetailUi
            ): Boolean = oldItem == newItem
        }
    }
}
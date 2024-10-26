package com.kjh.mynote.ui.features.place.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlacesSectionItemBinding
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailUiListAdapter(
    private val imageViewerClickAction: (List<String>, String) -> Unit,
    private val addressClickAction: (PlaceNoteModel) -> Unit,
    private val samePlaceItemClickAction: (PlaceNoteModel) -> Unit,
) : ListAdapter<PlaceNoteDetailUi, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.vh_place_note_detail_item -> {
            PlaceNoteDetailItemViewHolder(
                VhPlaceNoteDetailItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), imageViewerClickAction, addressClickAction
            )
        }
        R.layout.vh_place_note_detail_same_places_section_item -> {
            PlaceNoteDetailSamePlaceSectionItemViewHolder(
                VhPlaceNoteDetailSamePlacesSectionItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), samePlaceItemClickAction
            )
        }
        else -> throw Exception("Wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlaceNoteDetailUi.DetailItem ->
                (holder as PlaceNoteDetailItemViewHolder).bind(item)
            is PlaceNoteDetailUi.SamePlaceNameItem ->
                (holder as PlaceNoteDetailSamePlaceSectionItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PlaceNoteDetailUi.DetailItem -> R.layout.vh_place_note_detail_item
        is PlaceNoteDetailUi.SamePlaceNameItem -> R.layout.vh_place_note_detail_same_places_section_item
        else -> -1
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteDetailUi>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteDetailUi,
                newItem: PlaceNoteDetailUi
            ): Boolean = when {
                oldItem is PlaceNoteDetailUi.DetailItem && newItem is PlaceNoteDetailUi.DetailItem -> {
                    oldItem.placeNoteItem.id == newItem.placeNoteItem.id
                }
                oldItem is PlaceNoteDetailUi.SamePlaceNameItem && newItem is PlaceNoteDetailUi.SamePlaceNameItem -> {
                    oldItem.placeNoteItems == newItem.placeNoteItems
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: PlaceNoteDetailUi,
                newItem: PlaceNoteDetailUi
            ): Boolean = oldItem == newItem
        }
    }
}
package com.kjh.mynote.ui.features.place.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhPlaceNoteDetailItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteDetailPurchaseNotesOuterItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlacesOuterItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteDetailSectionTitleItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi
import com.kjh.mynote.ui.features.place.detail.adapter.detail.PlaceNoteDetailItemViewHolder
import com.kjh.mynote.ui.features.place.detail.adapter.purchasenotes.PlaceNoteDetailPurchaseNotesOuterViewHolder
import com.kjh.mynote.ui.features.place.detail.adapter.sameplaces.PlaceNoteDetailSamePlacesOuterViewHolder
import com.kjh.mynote.ui.features.place.detail.adapter.title.PlaceNoteDetailSectionTitleItemViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailUiListAdapter(
    private val imageViewerClickAction: (List<String>, String) -> Unit,
    private val addressClickAction: (PlaceNoteUiModel) -> Unit,
    private val samePlaceItemClickAction: (PlaceNoteUiModel) -> Unit,
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
) : ListAdapter<PlaceNoteDetailUi, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        // 장소노트 정보 Item.
        R.layout.vh_place_note_detail_item -> {
            PlaceNoteDetailItemViewHolder(
                VhPlaceNoteDetailItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), imageViewerClickAction, addressClickAction
            )
        }
        // Section Title Item.
        R.layout.vh_place_note_detail_section_title_item -> {
            PlaceNoteDetailSectionTitleItemViewHolder(
                VhPlaceNoteDetailSectionTitleItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
        // 다른 날 방문 목록 Item.
        R.layout.vh_place_note_detail_same_places_outer_item -> {
            PlaceNoteDetailSamePlacesOuterViewHolder(
                VhPlaceNoteDetailSamePlacesOuterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), samePlaceItemClickAction
            )
        }
        // 구매노트 목록 Item.
        R.layout.vh_place_note_detail_purchase_notes_outer_item -> {
            PlaceNoteDetailPurchaseNotesOuterViewHolder(
                VhPlaceNoteDetailPurchaseNotesOuterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }
        else -> throw Exception("Wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlaceNoteDetailUi.DetailItem ->
                (holder as PlaceNoteDetailItemViewHolder).bind(item)
            is PlaceNoteDetailUi.SectionTitleItem ->
                (holder as PlaceNoteDetailSectionTitleItemViewHolder).bind(item)
            is PlaceNoteDetailUi.SamePlaceNameItem ->
                (holder as PlaceNoteDetailSamePlacesOuterViewHolder).bind(item)
            is PlaceNoteDetailUi.PurchaseNoteItem ->
                (holder as PlaceNoteDetailPurchaseNotesOuterViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PlaceNoteDetailUi.DetailItem -> R.layout.vh_place_note_detail_item
        is PlaceNoteDetailUi.SectionTitleItem -> R.layout.vh_place_note_detail_section_title_item
        is PlaceNoteDetailUi.SamePlaceNameItem -> R.layout.vh_place_note_detail_same_places_outer_item
        is PlaceNoteDetailUi.PurchaseNoteItem -> R.layout.vh_place_note_detail_purchase_notes_outer_item
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
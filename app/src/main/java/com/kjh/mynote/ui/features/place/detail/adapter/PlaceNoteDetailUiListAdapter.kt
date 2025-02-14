package com.kjh.mynote.ui.features.place.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.databinding.VhPlaceNoteDetailInfoItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteDetailPurchaseNotesOuterItemBinding
import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlacesOuterItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUiItemState
import com.kjh.mynote.ui.features.place.detail.adapter.detail.PlaceNoteDetailInfoItemViewHolder
import com.kjh.mynote.ui.features.place.detail.adapter.purchasenotes.PlaceNoteDetailPurchaseNotesOuterViewHolder
import com.kjh.mynote.ui.features.place.detail.adapter.sameplaces.PlaceNoteDetailSamePlacesOuterViewHolder

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
) : ListAdapter<PlaceNoteDetailUiItemState, RecyclerView.ViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        // 장소노트 정보 Item.
        VIEW_TYPE_PLACE_INFO_ITEM -> {
            PlaceNoteDetailInfoItemViewHolder(
                VhPlaceNoteDetailInfoItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), imageViewerClickAction, addressClickAction
            )
        }
        // 구매노트 목록 Item.
        VIEW_TYPE_PURCHASE_NOTE_ITEMS -> {
            PlaceNoteDetailPurchaseNotesOuterViewHolder(
                VhPlaceNoteDetailPurchaseNotesOuterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), purchaseNoteItemClickAction
            )
        }
        // 다른 날 방문 목록 Item.
        VIEW_TYPE_SAME_PLACE_ITEMS -> {
            PlaceNoteDetailSamePlacesOuterViewHolder(
                VhPlaceNoteDetailSamePlacesOuterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ), samePlaceItemClickAction
            )
        }
        else -> throw Exception("Wrong viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlaceNoteDetailUiItemState.PlaceDetailInfoItem ->
                (holder as PlaceNoteDetailInfoItemViewHolder).bind(item)
            is PlaceNoteDetailUiItemState.SamePlaceNameNotesItem ->
                (holder as PlaceNoteDetailSamePlacesOuterViewHolder).bind(item)
            is PlaceNoteDetailUiItemState.PurchaseNotesItem ->
                (holder as PlaceNoteDetailPurchaseNotesOuterViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PlaceNoteDetailUiItemState.PlaceDetailInfoItem -> VIEW_TYPE_PLACE_INFO_ITEM
        is PlaceNoteDetailUiItemState.PurchaseNotesItem -> VIEW_TYPE_PURCHASE_NOTE_ITEMS
        is PlaceNoteDetailUiItemState.SamePlaceNameNotesItem -> VIEW_TYPE_SAME_PLACE_ITEMS
    }

    companion object {
        private const val VIEW_TYPE_PLACE_INFO_ITEM = 1
        private const val VIEW_TYPE_PURCHASE_NOTE_ITEMS = 2
        private const val VIEW_TYPE_SAME_PLACE_ITEMS = 3

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteDetailUiItemState>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteDetailUiItemState,
                newItem: PlaceNoteDetailUiItemState
            ): Boolean = when {
                oldItem is PlaceNoteDetailUiItemState.PlaceDetailInfoItem
                        && newItem is PlaceNoteDetailUiItemState.PlaceDetailInfoItem -> {
                    oldItem.placeNoteItem.id == newItem.placeNoteItem.id
                }
                oldItem is PlaceNoteDetailUiItemState.SamePlaceNameNotesItem
                        && newItem is PlaceNoteDetailUiItemState.SamePlaceNameNotesItem -> {
                    true
                }
                oldItem is PlaceNoteDetailUiItemState.PurchaseNotesItem
                        && newItem is PlaceNoteDetailUiItemState.PurchaseNotesItem -> {
                    true
                }
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: PlaceNoteDetailUiItemState,
                newItem: PlaceNoteDetailUiItemState,
            ): Boolean = when {
                oldItem is PlaceNoteDetailUiItemState.PurchaseNotesItem
                        && newItem is PlaceNoteDetailUiItemState.PurchaseNotesItem -> {
                    oldItem.purchaseNoteItems == newItem.purchaseNoteItems
                }

                oldItem is PlaceNoteDetailUiItemState.SamePlaceNameNotesItem
                        && newItem is PlaceNoteDetailUiItemState.SamePlaceNameNotesItem -> {
                    oldItem.placeNoteItems.map { it.placeImages[0] } == newItem.placeNoteItems.map { it.placeImages[0] }
                            && oldItem.placeNoteItems.map { it.visitDate } == newItem.placeNoteItems.map { it.visitDate }
                }

                else -> {
                    oldItem == newItem
                }
            }
        }
    }
}
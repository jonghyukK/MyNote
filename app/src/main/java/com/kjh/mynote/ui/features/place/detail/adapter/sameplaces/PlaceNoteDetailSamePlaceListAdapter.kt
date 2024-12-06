package com.kjh.mynote.ui.features.place.detail.adapter.sameplaces

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlaceItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 24..
 * Description:
 */
class PlaceNoteDetailSamePlaceListAdapter(
    private val samePlaceItemClickAction: (PlaceNoteUiModel) -> Unit
) : ListAdapter<PlaceNoteUiModel, PlaceNoteDetailSamePlaceItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaceNoteDetailSamePlaceItemViewHolder(
        VhPlaceNoteDetailSamePlaceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), samePlaceItemClickAction
    )

    override fun onBindViewHolder(holder: PlaceNoteDetailSamePlaceItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceNoteUiModel>() {
            override fun areItemsTheSame(
                oldItem: PlaceNoteUiModel,
                newItem: PlaceNoteUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PlaceNoteUiModel,
                newItem: PlaceNoteUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
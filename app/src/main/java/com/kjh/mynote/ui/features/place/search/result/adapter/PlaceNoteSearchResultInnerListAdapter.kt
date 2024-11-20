package com.kjh.mynote.ui.features.place.search.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteSearchResultInnerItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class PlaceNoteSearchResultInnerListAdapter(
    private val onClickAction: (PlaceNoteUiModel) -> Unit
) : ListAdapter<PlaceNoteUiModel, PlaceNoteSearchResultInnerItemViewHolder>(
    UI_MODEL_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaceNoteSearchResultInnerItemViewHolder(
        VhPlaceNoteSearchResultInnerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), onClickAction
    )

    override fun onBindViewHolder(
        holder: PlaceNoteSearchResultInnerItemViewHolder,
        position: Int
    ) {
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

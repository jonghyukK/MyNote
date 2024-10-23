package com.kjh.mynote.ui.features.place.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPlaceNoteDetailPagerImageItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */
class PlaceNoteDetailPagerAdapter(
    private val imageClickAction: (String) -> Unit
): ListAdapter<String, PlaceNoteDetailPagerItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaceNoteDetailPagerItemViewHolder(
        VhPlaceNoteDetailPagerImageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), imageClickAction
    )
    override fun onBindViewHolder(holder: PlaceNoteDetailPagerItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem
        }
    }
}
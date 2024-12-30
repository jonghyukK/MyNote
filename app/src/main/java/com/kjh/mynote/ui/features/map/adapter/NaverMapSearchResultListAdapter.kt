package com.kjh.mynote.ui.features.map.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhNaverMapSearchResultItemBinding
import com.kjh.mynote.model.PlaceInfoUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 7..
 * Description:
 */
class NaverMapSearchResultListAdapter(
    private val onPlaceClickAction: (PlaceInfoUiModel) -> Unit,
    private val onSelectClickAction: (PlaceInfoUiModel) -> Unit
): ListAdapter<PlaceInfoUiModel, NaverMapSearchResultItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = NaverMapSearchResultItemViewHolder(
        VhNaverMapSearchResultItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), onPlaceClickAction, onSelectClickAction
    )

    override fun onBindViewHolder(holder: NaverMapSearchResultItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<PlaceInfoUiModel>() {
            override fun areItemsTheSame(
                oldItem: PlaceInfoUiModel,
                newItem: PlaceInfoUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PlaceInfoUiModel,
                newItem: PlaceInfoUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
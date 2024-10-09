package com.kjh.mynote.ui.features.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.mynote.databinding.VhNaverMapSearchResultItemBinding

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 7..
 * Description:
 */
class NaverMapSearchResultListAdapter(
    private val onPlaceClickAction: (KakaoPlaceModel) -> Unit,
    private val onSelectClickAction: (KakaoPlaceModel) -> Unit
): ListAdapter<KakaoPlaceModel, NaverMapSearchResultItemViewHolder>(UI_MODEL_COMPARATOR) {

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
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<KakaoPlaceModel>() {
            override fun areItemsTheSame(
                oldItem: KakaoPlaceModel,
                newItem: KakaoPlaceModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: KakaoPlaceModel,
                newItem: KakaoPlaceModel
            ): Boolean = oldItem == newItem
        }
    }
}
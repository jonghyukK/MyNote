package com.kjh.mynote.ui.features.map

import com.kjh.mynote.databinding.VhNaverMapSearchResultItemBinding
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 7..
 * Description:
 */

class NaverMapSearchResultItemViewHolder(
    private val binding: VhNaverMapSearchResultItemBinding,
    private val onPlaceClickAction: (PlaceInfoUiModel) -> Unit,
    private val onSelectClickAction: (PlaceInfoUiModel) -> Unit
): BaseViewHolder<PlaceInfoUiModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onPlaceClickAction.invoke(item) }
        }

        binding.btnSelect.onThrottleClick {
            bindItem?.let { item -> onSelectClickAction.invoke(item) }
        }
    }

    override fun bind(item: PlaceInfoUiModel) {
        super.bind(item)

        with (binding) {
            tvPlaceName.text = item.placeName
            tvAddress.text = item.address
            tvRoadAddress.text = item.roadAddress
        }
    }
}
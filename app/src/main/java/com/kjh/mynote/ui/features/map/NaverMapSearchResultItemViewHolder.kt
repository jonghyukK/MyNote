package com.kjh.mynote.ui.features.map

import com.kjh.data.model.KakaoPlaceModel
import com.kjh.mynote.databinding.VhNaverMapSearchResultItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 7..
 * Description:
 */

class NaverMapSearchResultItemViewHolder(
    private val binding: VhNaverMapSearchResultItemBinding,
    private val onPlaceClickAction: (KakaoPlaceModel) -> Unit,
    private val onSelectClickAction: (KakaoPlaceModel) -> Unit
): BaseViewHolder<KakaoPlaceModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> onPlaceClickAction.invoke(item) }
        }

        binding.btnSelect.onThrottleClick {
            bindItem?.let { item -> onSelectClickAction.invoke(item) }
        }
    }

    override fun bind(item: KakaoPlaceModel) {
        super.bind(item)

        with (binding) {
            tvPlaceName.text = item.placeName
            tvAddress.text = item.addressName
            tvRoadAddress.text = item.roadAddressName
        }
    }
}
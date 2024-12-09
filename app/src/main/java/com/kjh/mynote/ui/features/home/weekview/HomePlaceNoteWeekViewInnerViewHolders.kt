package com.kjh.mynote.ui.features.home.weekview

import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.databinding.VhHomePlaceeNoteWeekViewPlaceNoteItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

class HomePlaceNoteWeekViewInnerPlaceNoteItemViewHolder(
    private val binding: VhHomePlaceeNoteWeekViewPlaceNoteItemBinding,
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteUiModel>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> placeNoteClickAction(item) }
        }
    }

    override fun bind(item: PlaceNoteUiModel) {
        super.bind(item)

        with (binding) {
            with (binding) {
                ivImage.loadImage(item.placeImages[0])
                tvPlaceName.text = item.placeInfo.placeName
                tvPlaceArea.text = item.placeInfo.placeRegion
            }
        }
    }
}

class HomePlaceNoteWeekViewInnerEmptyItemViewHolder(
    private val binding: LayoutEmptyMyPlacesBinding
): BaseViewHolder<Unit>(binding.root) {


}

class HomePlaceNoteWeekViewInnerMoreItemViewHolder(
    private val binding: VhHomePlaceNoteWeekViewInnerMoreItemBinding,
): BaseViewHolder<Unit>(binding.root) {

}
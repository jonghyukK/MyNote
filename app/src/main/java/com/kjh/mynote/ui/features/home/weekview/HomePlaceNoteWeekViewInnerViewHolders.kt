package com.kjh.mynote.ui.features.home.weekview

import com.kjh.mynote.databinding.LayoutEmptyMyPlacesBinding
import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewInnerMoreItemBinding
import com.kjh.mynote.databinding.VhHomePlaceeNoteWeekViewPlaceNoteItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.addClickAnimation
import com.kjh.mynote.utils.extensions.dpToPx
import com.kjh.mynote.utils.extensions.loadImage
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

/**
 * Home PlaceNote Item ViewHolder.
 *
 * @property binding
 * @property placeNoteClickAction
 */
class HomePlaceNoteWeekViewInnerPlaceNoteItemViewHolder(
    private val binding: VhHomePlaceeNoteWeekViewPlaceNoteItemBinding,
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteUiModel>(binding.root) {

    init {
        itemView.apply {
            onThrottleClick {
                bindItem?.let { item -> placeNoteClickAction(item) }
            }
            addClickAnimation()
        }
    }

    override fun bind(item: PlaceNoteUiModel) {
        super.bind(item)

        with (binding) {
            ivImage.loadImage(item.placeImages[0])
            tvPlaceName.text = item.placeInfo.placeName
            tvPlaceArea.text = item.placeInfo.placeRegion
        }
    }
}

/**
 * Home PlaceNote Empty ViewHolder.
 *
 * @property binding
 */
class HomePlaceNoteWeekViewInnerEmptyItemViewHolder(
    private val binding: LayoutEmptyMyPlacesBinding,
    private val makePlaceNoteClickAction: () -> Unit
): BaseViewHolder<Unit>(binding.root) {

    init {
        binding.btnMakePlace.onThrottleClick {
            bindItem?.let { makePlaceNoteClickAction() }
        }
    }

    override fun bind(item: Unit) {
        super.bind(item)

        val layoutParams = itemView.layoutParams
        layoutParams.height = 200.dpToPx()
        itemView.layoutParams = layoutParams
    }
}

/**
 * Home PlaceNote More Button ViewHolder.
 *
 * @property binding
 */
class HomePlaceNoteWeekViewInnerMoreItemViewHolder(
    private val binding: VhHomePlaceNoteWeekViewInnerMoreItemBinding,
): BaseViewHolder<Unit>(binding.root) {

}
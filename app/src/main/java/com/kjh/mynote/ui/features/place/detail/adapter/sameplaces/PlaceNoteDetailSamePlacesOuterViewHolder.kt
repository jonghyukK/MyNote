package com.kjh.mynote.ui.features.place.detail.adapter.sameplaces

import androidx.recyclerview.widget.PagerSnapHelper
import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlacesOuterItemBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi
import com.kjh.mynote.utils.decorations.SpacingItemDecoration

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 24..
 * Description:
 */
class PlaceNoteDetailSamePlacesOuterViewHolder(
    private val binding: VhPlaceNoteDetailSamePlacesOuterItemBinding,
    private val samePlaceItemClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteDetailUi.SamePlaceNameItem>(binding.root) {

    private val listAdapter = PlaceNoteDetailSamePlaceListAdapter(samePlaceItemClickAction)
    private val pagerSnapHelper = PagerSnapHelper()

    init {
        binding.rvSamePlaces.apply {
            adapter = listAdapter
            if (onFlingListener == null) {
                pagerSnapHelper.attachToRecyclerView(this)
            }
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(left = 12))
            }
        }
    }

    override fun bind(item: PlaceNoteDetailUi.SamePlaceNameItem) {
        super.bind(item)

        listAdapter.submitList(null)
        listAdapter.submitList(item.placeNoteItems)
    }
}
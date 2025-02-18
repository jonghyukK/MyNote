package com.kjh.mynote.ui.features.place.search.result.adapter

import com.kjh.mynote.databinding.VhPlaceNoteSearchResultOuterItemBinding
import com.kjh.mynote.model.FilteredSearchPlaceNotesUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.toStringWithPattern

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class PlaceNoteSearchResultOuterItemViewHolder(
    private val binding: VhPlaceNoteSearchResultOuterItemBinding,
    private val placeNoteItemClickAction: (PlaceNoteUiModel) -> Unit
): BaseViewHolder<FilteredSearchPlaceNotesUiModel>(binding.root) {

    private val innerListAdapter: PlaceNoteSearchResultInnerListAdapter =
        PlaceNoteSearchResultInnerListAdapter(placeNoteItemClickAction)

    init {
        binding.rvPlaceNotes.apply {
            itemAnimator = null
            adapter = innerListAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(top = 12))
            }
        }
    }

    override fun bind(item: FilteredSearchPlaceNotesUiModel) {
        super.bind(item)

        with (binding) {
            tvDate.text = item.date.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M_D_E)

            innerListAdapter.submitList(item.placeNoteItems)
        }
    }
}

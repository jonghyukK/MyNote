package com.kjh.mynote.ui.features.place.detail.adapter

import androidx.recyclerview.widget.PagerSnapHelper
import com.kjh.data.model.PlaceNoteModel
import com.kjh.mynote.databinding.VhPlaceNoteDetailSamePlacesSectionItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi
import com.kjh.mynote.utils.SpacingItemDecoration
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 24..
 * Description:
 */
class PlaceNoteDetailSamePlaceSectionItemViewHolder(
    private val binding: VhPlaceNoteDetailSamePlacesSectionItemBinding,
    private val samePlaceItemClickAction: (PlaceNoteModel) -> Unit
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

        binding.tvSectionTitle.text = item.title
        listAdapter.submitList(null)
        listAdapter.submitList(item.placeNoteItems)
    }
}
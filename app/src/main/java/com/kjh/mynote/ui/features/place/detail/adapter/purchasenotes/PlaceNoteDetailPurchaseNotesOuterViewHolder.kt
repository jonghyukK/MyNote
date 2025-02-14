package com.kjh.mynote.ui.features.place.detail.adapter.purchasenotes

import com.kjh.mynote.databinding.VhPlaceNoteDetailPurchaseNotesOuterItemBinding
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUiItemState
import com.kjh.mynote.utils.decorations.SpacingItemDecoration

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 6..
 * Description:
 */
class PlaceNoteDetailPurchaseNotesOuterViewHolder(
    private val binding: VhPlaceNoteDetailPurchaseNotesOuterItemBinding,
    private val purchaseNoteItemClickAction: (PurchaseNoteUiModel) -> Unit
): BaseViewHolder<PlaceNoteDetailUiItemState.PurchaseNotesItem>(binding.root) {

    private val innerListAdapter = PlaceNoteDetailPurchaseNoteListAdapter(purchaseNoteItemClickAction)

    init {
        binding.rvPurchaseNotes.apply {
            itemAnimator = null
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(top = 10))
            }
            adapter = innerListAdapter
        }
    }

    override fun bind(item: PlaceNoteDetailUiItemState.PurchaseNotesItem) {
        super.bind(item)
        innerListAdapter.submitList(item.purchaseNoteItems)
    }
}
package com.kjh.mynote.ui.features.place.detail.adapter.title

import com.kjh.mynote.databinding.VhPlaceNoteDetailSectionTitleItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailUi

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 6..
 * Description:
 */

class PlaceNoteDetailSectionTitleItemViewHolder(
    private val binding: VhPlaceNoteDetailSectionTitleItemBinding
): BaseViewHolder<PlaceNoteDetailUi.SectionTitleItem>(binding.root) {

    override fun bind(item: PlaceNoteDetailUi.SectionTitleItem) {
        super.bind(item)

        binding.tvSectionTitle.text = item.sectionTitle
    }
}
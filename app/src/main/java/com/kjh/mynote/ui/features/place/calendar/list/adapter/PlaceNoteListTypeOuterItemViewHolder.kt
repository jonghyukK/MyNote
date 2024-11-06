package com.kjh.mynote.ui.features.place.calendar.list.adapter

import com.kjh.mynote.databinding.VhPlaceNoteInCalendarListTypeBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.calendar.MonthWithPlaceNotesItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

class PlaceNoteListTypeOuterItemViewHolder(
    val binding: VhPlaceNoteInCalendarListTypeBinding,
    private val placeItemClickAction: (PlaceNoteUiModel) -> Unit,
    private val makeNoteClickAction: () -> Unit
): BaseViewHolder<MonthWithPlaceNotesItem>(binding.root) {

    private val innerListAdapter = PlaceNoteListTypeInnerAdapter(placeItemClickAction, makeNoteClickAction)

    init {
        binding.rvList.apply {
            itemAnimator = null
            adapter = innerListAdapter
        }
    }

    override fun bind(item: MonthWithPlaceNotesItem) {
        super.bind(item)
        innerListAdapter.submitList(item.placeNoteItems)
    }
}

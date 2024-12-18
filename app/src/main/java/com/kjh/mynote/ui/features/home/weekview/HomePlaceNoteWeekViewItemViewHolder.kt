package com.kjh.mynote.ui.features.home.weekview

import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.home.HomeUiState
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

class HomePlaceNoteWeekViewItemViewHolder(
    private val binding: VhHomePlaceNoteWeekViewBinding,
    private val weekDayClickAction: (LocalDate) -> Unit,
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit,
    private val makePlaceNoteClickAction: () -> Unit
): BaseViewHolder<HomeUiState.PlaceNoteWeekItem>(binding.root) {

    private val innerListAdapter = HomePlaceNoteWeekViewInnerAdapter(
        placeNoteClickAction = placeNoteClickAction,
        makePlaceNoteClickAction = makePlaceNoteClickAction
    )

    init {
        binding.rvPlaceNotes.apply {
            itemAnimator = null
            adapter = innerListAdapter
        }

        binding.calendarWeekView.apply {
            setDayClickAction(weekDayClickAction)
        }
    }

    override fun bind(item: HomeUiState.PlaceNoteWeekItem) {
        super.bind(item)

        binding.calendarWeekView.updateSelectDayWithEventDates(
            item.selectedDate to item.eventDays)

        innerListAdapter.submitList(item.displayedPlaceNotes)
    }
}
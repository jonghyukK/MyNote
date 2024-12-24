package com.kjh.mynote.ui.features.home.adapter.weeklyplacenotes

import com.kjh.mynote.databinding.VhHomePlaceNoteWeekViewBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.home.HomeUiItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

class HomeWeeklyPlaceNoteSectionItemViewHolder(
    private val binding: VhHomePlaceNoteWeekViewBinding,
    private val weekDayClickAction: (LocalDate) -> Unit,
    private val placeNoteClickAction: (PlaceNoteUiModel) -> Unit,
    private val makePlaceNoteClickAction: () -> Unit,
    private val seeAllPlaceNotesClickAction: () -> Unit
): BaseViewHolder<HomeUiItem.HomeWeeklyPurchaseNoteItem>(binding.root) {

    private val innerListAdapter = HomeWeeklyPlaceNoteListAdapter(
        placeNoteClickAction = placeNoteClickAction,
        makePlaceNoteClickAction = makePlaceNoteClickAction,
        seeAllPlaceNotesClickAction = seeAllPlaceNotesClickAction
    )

    init {
        binding.clShowAllPlaceNotes.onThrottleClick {
            bindItem?.let { seeAllPlaceNotesClickAction() }
        }

        binding.rvPlaceNotes.apply {
            itemAnimator = null
            adapter = innerListAdapter
        }

        binding.calendarWeekView.apply {
            setDayClickAction(weekDayClickAction)
        }
    }

    override fun bind(item: HomeUiItem.HomeWeeklyPurchaseNoteItem) {
        super.bind(item)

        binding.calendarWeekView.updateSelectDayWithEventDates(
            item.selectedDate to item.eventExistingDays)

        innerListAdapter.submitList(item.placeNoteUiItems)
    }
}
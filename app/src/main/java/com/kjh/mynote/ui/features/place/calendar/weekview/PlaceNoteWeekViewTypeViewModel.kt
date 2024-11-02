package com.kjh.mynote.ui.features.place.calendar.weekview

import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetPlaceNotesUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

data class WeekViewTypeUiState(
    val selectedDay: LocalDate = LocalDate.now(),
    val selectedDayPlaceNoteItems: List<WeekViewTypeCalendarPlaceNoteUI> = emptyList(),
    val selectedMonthEventDays: List<LocalDate> = emptyList()
)

sealed class WeekViewTypeCalendarPlaceNoteUI {
    data class OnePicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        WeekViewTypeCalendarPlaceNoteUI()

    data class TwoPicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        WeekViewTypeCalendarPlaceNoteUI()

    data class ThreePicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        WeekViewTypeCalendarPlaceNoteUI()

    data class FourPicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        WeekViewTypeCalendarPlaceNoteUI()

    data class OverPicturePlaceNoteItem(
        val item: PlaceNoteUiModel,
        val remainImgCount: Int
    ) : WeekViewTypeCalendarPlaceNoteUI()

    object EmptyItem: WeekViewTypeCalendarPlaceNoteUI()
}

@HiltViewModel
class PlaceNoteWeekViewTypeViewModel @Inject constructor(
    private val getPlaceNotesUseCase: GetPlaceNotesUseCase
): BaseViewModel() {

    private val _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())

    val groupedNotesByDateFlow = getPlaceNotesUseCase()
        .map { notes -> notes.toUiModel().groupBy { it.localDate } }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyMap()
        )

    val uiState = combine(
        _selectedDay, groupedNotesByDateFlow
    ) { selectedDay, notesMap ->
        val notesInDay = notesMap[selectedDay] ?: emptyList()
        val eventDatesInMonth = filterNotesByMonth(notesMap.keys, selectedDay)

        WeekViewTypeUiState(
            selectedDay = selectedDay,
            selectedDayPlaceNoteItems = makeWeekViewTypePlaceNoteItems(notesInDay),
            selectedMonthEventDays = eventDatesInMonth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeekViewTypeUiState()
    )

    fun setSelectedDay(date: LocalDate) {
        if (date == _selectedDay.value) return
        _selectedDay.value = date
    }

    fun getSelectedDate() = _selectedDay.value

    private fun filterNotesByMonth(
        dates: Set<LocalDate>,
        selectedDay: LocalDate
    ): List<LocalDate> {
        return dates.filter { date -> date.year == selectedDay.year && date.month == selectedDay.month }
    }

    private fun makeWeekViewTypePlaceNoteItems(placeNotes: List<PlaceNoteUiModel>): List<WeekViewTypeCalendarPlaceNoteUI> {
        if (placeNotes.isEmpty()) {
            return listOf(WeekViewTypeCalendarPlaceNoteUI.EmptyItem)
        }

        return placeNotes.map { note ->
            when (note.placeImages.size) {
                1 -> WeekViewTypeCalendarPlaceNoteUI.OnePicturePlaceNoteItem(note)
                2 -> WeekViewTypeCalendarPlaceNoteUI.TwoPicturePlaceNoteItem(note)
                3 -> WeekViewTypeCalendarPlaceNoteUI.ThreePicturePlaceNoteItem(note)
                4 -> WeekViewTypeCalendarPlaceNoteUI.FourPicturePlaceNoteItem(note)
                else -> WeekViewTypeCalendarPlaceNoteUI.OverPicturePlaceNoteItem(
                    item = note,
                    remainImgCount = note.placeImages.size - 5
                )
            }
        }
    }
}
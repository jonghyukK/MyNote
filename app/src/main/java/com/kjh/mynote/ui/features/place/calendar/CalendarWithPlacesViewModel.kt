package com.kjh.mynote.ui.features.place.calendar

import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetPlaceNotesUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

data class CalendarUiState(
    val selectedDay: LocalDate = LocalDate.now(),
    val selectedDateUiText: String = selectedDay.toStringWithPattern("yyyy년 MM월"),
    val selectedDayPlaceNoteItems: List<CalendarPlaceNoteUiState> = emptyList(),
    val selectedMonthEventDays: List<LocalDate> = emptyList()
)

sealed class CalendarPlaceNoteUiState {
    data class OnePicturePlaceNoteItem(
        val item: PlaceNoteUiModel
    ): CalendarPlaceNoteUiState()

    data class TwoPicturePlaceNoteItem(
        val item: PlaceNoteUiModel
    ): CalendarPlaceNoteUiState()

    data class ThreePicturePlaceNoteItem(
        val item: PlaceNoteUiModel
    ): CalendarPlaceNoteUiState()

    data class FourPicturePlaceNoteItem(
        val item: PlaceNoteUiModel
    ): CalendarPlaceNoteUiState()

    data class OverPicturePlaceNoteItem(
        val item: PlaceNoteUiModel,
        val remainImgCount: Int
    ): CalendarPlaceNoteUiState()
}

@HiltViewModel
class CalendarWithPlacesViewModel @Inject constructor(
    private val getPlaceNotesUseCase: GetPlaceNotesUseCase
): BaseViewModel() {

    private val _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())

    val allPlaceNotesFlow = getPlaceNotesUseCase()
        .map { notes -> notes.toUiModel().groupBy { it.localDate } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyMap()
        )

    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDay, allPlaceNotesFlow
    ) { selectedDay, allNotes ->
        CalendarUiState(
            selectedDay = selectedDay,
            selectedDateUiText = selectedDay.toStringWithPattern("yyyy년 MM월"),
            selectedDayPlaceNoteItems = allNotes[selectedDay]?.map {
                makeCalendarPlaceNoteUiItem(it)
            } ?: emptyList(),
            selectedMonthEventDays = filterNotesByMonth(allNotes.keys, selectedDay)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CalendarUiState()
    )

    fun selectDay(newDay: LocalDate) {
        _selectedDay.value = newDay
    }

    fun getSelectedDate() = _selectedDay.value

    private fun filterNotesByMonth(
        dates: Set<LocalDate>,
        selectedDay: LocalDate
    ): List<LocalDate> {
        return dates.filter { date -> date.year == selectedDay.year && date.month == selectedDay.month }
    }

    private fun makeCalendarPlaceNoteUiItem(placeNoteItem: PlaceNoteUiModel): CalendarPlaceNoteUiState {
        return when (placeNoteItem.placeImages.size) {
            1 -> CalendarPlaceNoteUiState.OnePicturePlaceNoteItem(placeNoteItem)
            2 -> CalendarPlaceNoteUiState.TwoPicturePlaceNoteItem(placeNoteItem)
            3 -> CalendarPlaceNoteUiState.ThreePicturePlaceNoteItem(placeNoteItem)
            4 -> CalendarPlaceNoteUiState.FourPicturePlaceNoteItem(placeNoteItem)
            else -> CalendarPlaceNoteUiState.OverPicturePlaceNoteItem(
                item = placeNoteItem,
                remainImgCount = placeNoteItem.placeImages.size - 5
            )
        }
    }
}



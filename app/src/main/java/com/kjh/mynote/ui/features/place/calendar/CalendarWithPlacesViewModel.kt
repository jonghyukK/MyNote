package com.kjh.mynote.ui.features.place.calendar

import androidx.lifecycle.viewModelScope
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

data class CalendarUiState(
    val selectedDay: LocalDate = LocalDate.now(),
    val currentYearMonthText: String = LocalDate.now().toStringWithPattern("yyyy년 MM월")
)

sealed class CalendarPlaceNoteUiState {
    data class OnePicturePlaceNoteItem(
        val item: PlaceNoteModel
    ): CalendarPlaceNoteUiState()

    data class TwoPicturePlaceNoteItem(
        val item: PlaceNoteModel
    ): CalendarPlaceNoteUiState()

    data class ThreePicturePlaceNoteItem(
        val item: PlaceNoteModel
    ): CalendarPlaceNoteUiState()

    data class FourPicturePlaceNoteItem(
        val item: PlaceNoteModel
    ): CalendarPlaceNoteUiState()

    data class OverPicturePlaceNoteItem(
        val item: PlaceNoteModel,
        val remainImgCount: Int
    ): CalendarPlaceNoteUiState()
}

@HiltViewModel
class CalendarWithPlacesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    val allPlaceNotesFlow = noteRepository.observeAll()
        .map { notes -> notes.groupBy { it.localDate } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyMap()
        )

    val thisMonthDatesWithNotesFlow = combine(
        _uiState, allPlaceNotesFlow
    ) { uiState, allNotes ->
        val selectedDay = uiState.selectedDay
        selectedDay to filterNotesByMonth(allNotes.keys, selectedDay)
    }

    val selectedDayPlaceNotesFlow = combine(
        thisMonthDatesWithNotesFlow, allPlaceNotesFlow
    ) { dateWithEventsPair, allNotes ->
        allNotes[dateWithEventsPair.first]?.map {
            makeCalendarPlaceNoteUiItem(it)
        } ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun selectDay(newDay: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDay = newDay,
                currentYearMonthText = newDay.toStringWithPattern("yyyy년 MM월")
            )
        }
    }

    fun getSelectedDate() = _uiState.value.selectedDay

    private fun filterNotesByMonth(
        dates: Set<LocalDate>,
        selectedDay: LocalDate
    ): List<LocalDate> {
        return dates.filter { date -> date.year == selectedDay.year && date.month == selectedDay.month }
    }

    private fun makeCalendarPlaceNoteUiItem(placeNoteItem: PlaceNoteModel): CalendarPlaceNoteUiState {
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



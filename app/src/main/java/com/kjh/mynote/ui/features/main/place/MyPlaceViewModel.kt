package com.kjh.mynote.ui.features.main.place

import androidx.lifecycle.viewModelScope
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.CalendarUtils.getStartAndEndOfMonth
import com.kjh.mynote.utils.CalendarUtils.localDateToStringWithPattern
import com.kjh.mynote.utils.CalendarUtils.millisToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

data class CalendarUiState(
    val selectedDay: LocalDate = LocalDate.now(),
    val currentYearMonthText: String = localDateToStringWithPattern(LocalDate.now(), "yyyy년 MM월")
)

sealed class CalendarUiEvent {
    data class Initialize(
        val hasNoteDaysMap: Map<LocalDate, List<PlaceNoteModel>>
    ): CalendarUiEvent()

    data class UpdateDayToDay(
        val oldDate: LocalDate,
        val newDate: LocalDate
    ): CalendarUiEvent()

    data class UpdateMonthToMonth(
        val newDate: LocalDate
    ): CalendarUiEvent()
}

@HiltViewModel
class MyPlaceViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    private val _calendarUiEvent = MutableSharedFlow<CalendarUiEvent>()
    val calendarUiEvent = _calendarUiEvent.asSharedFlow()

    private val _wholePlacesMap = MutableStateFlow<Map<LocalDate, List<PlaceNoteModel>>>(emptyMap())
    val wholePlaceMap = _wholePlacesMap.asStateFlow()

    val placeNotesByDayFlow = combine(
        _uiState, _wholePlacesMap
    ) { uiState, wholePlaceMap ->
        wholePlaceMap[uiState.selectedDay] ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun getPlacesByMonth(
        localDate: LocalDate,
        isInit: Boolean = false,
        withSelectedDayUpdate: Boolean = false
    ) {
        val (startDateMillis, endDateMillis) = getStartAndEndOfMonth(
            localDate.year, localDate.monthValue
        )

        viewModelScope.launch {
            noteRepository.getPlacesInDateRange(
                startDate = startDateMillis,
                endDate = endDateMillis
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        val placesInMonth = result.data ?: emptyList()
                        val map = placesInMonth.distinct().groupBy { millisToLocalDate(it.visitDate) }

                        _wholePlacesMap.update {
                            it + map
                        }

                        if (withSelectedDayUpdate || isInit) {
                            changeSelectedDay(localDate, isInit)
                        }
                    }
                    is Result.Error -> {}
                }
            }
        }
    }

    fun changeSelectedDay(newDay: LocalDate, isInit: Boolean = false) {
        val oldDate = _uiState.value.selectedDay
        val isYearOrMonthDifferent = oldDate.year != newDay.year || oldDate.month != newDay.month

        _uiState.update {
            it.copy(
                selectedDay = newDay,
                currentYearMonthText = localDateToStringWithPattern(newDay, "yyyy년 MM월")
            )
        }

        viewModelScope.launch {
            when {
                isInit ->
                    _calendarUiEvent.emit(CalendarUiEvent.Initialize(_wholePlacesMap.value))
                isYearOrMonthDifferent ->
                    _calendarUiEvent.emit(CalendarUiEvent.UpdateMonthToMonth(newDay))
                else ->
                    _calendarUiEvent.emit(CalendarUiEvent.UpdateDayToDay(oldDate, newDay))
            }
        }
    }

    fun getSelectedDate() = _uiState.value.selectedDay

    fun checkNoteInDay(day: LocalDate): Boolean {
        return !_wholePlacesMap.value[day].isNullOrEmpty()
    }
}



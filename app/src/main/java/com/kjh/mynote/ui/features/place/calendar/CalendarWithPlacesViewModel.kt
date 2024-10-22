package com.kjh.mynote.ui.features.place.calendar

import androidx.lifecycle.viewModelScope
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.CalendarUtils.getStartAndEndOfMonth
import com.kjh.mynote.utils.extensions.toLocalDate
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

sealed class CalendarUiEvent {
    data class Initialize(
        val hasNoteDaysMap: Map<LocalDate, List<PlaceNoteModel>>
    ): CalendarUiEvent()

    data class UpdateDayInDay(
        val currentDate: LocalDate
    ): CalendarUiEvent()

    data class UpdateDayToDay(
        val oldDate: LocalDate,
        val newDate: LocalDate
    ): CalendarUiEvent()

    data class UpdateMonthToMonth(
        val newDate: LocalDate
    ): CalendarUiEvent()
}

sealed class CalendarPlaceNoteUiState {

    data class OnePicturePlaceNoteItem(val item: PlaceNoteModel): CalendarPlaceNoteUiState()

    data class TwoPicturePlaceNoteItem(val item: PlaceNoteModel): CalendarPlaceNoteUiState()

    data class ThreePicturePlaceNoteItem(val item: PlaceNoteModel): CalendarPlaceNoteUiState()

    data class FourPicturePlaceNoteItem(val item: PlaceNoteModel): CalendarPlaceNoteUiState()

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

    private val _calendarUiEvent = MutableSharedFlow<CalendarUiEvent>()
    val calendarUiEvent = _calendarUiEvent.asSharedFlow()

    private val _wholePlacesMap = MutableStateFlow<Map<LocalDate, List<PlaceNoteModel>>>(emptyMap())
    val wholePlaceMap = _wholePlacesMap.asStateFlow()

    val placeNotesByDayFlow = combine(
        _uiState, _wholePlacesMap
    ) { uiState, wholePlaceMap ->
        wholePlaceMap[uiState.selectedDay]?.map {
            makeCalendarPlaceNoteUiItem(it)
        } ?: emptyList()
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
                        val map = placesInMonth.distinct().groupBy { it.visitDate.toLocalDate() }

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
                currentYearMonthText = newDay.toStringWithPattern("yyyy년 MM월")
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

    fun deleteAndUpdateWholePlaceMap(deletedNoteId: Int) {
        val filteredWholePlaceMap = _wholePlacesMap.value.mapValues { entry ->
            entry.value.filter { it.id != deletedNoteId }
        }

        val deletedMapKey = filteredWholePlaceMap.filterValues { it.isEmpty() }.keys
        if (deletedMapKey.isNotEmpty()) {
            viewModelScope.launch {
                _calendarUiEvent.emit(CalendarUiEvent.UpdateDayInDay(deletedMapKey.first()))
            }
        }

        _wholePlacesMap.value = filteredWholePlaceMap.filterValues { it.isNotEmpty() }
    }

    fun updateWholePlaceMap(placeItem: PlaceNoteModel) {
        val filteredWholePlaceMap = _wholePlacesMap.value.mapValues { entry ->
            entry.value.filter { it.id != placeItem.id }
        }.filterValues { it.isNotEmpty() }

        _wholePlacesMap.value = filteredWholePlaceMap

        getPlacesByMonth(placeItem.visitDate.toLocalDate(), withSelectedDayUpdate = true)
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



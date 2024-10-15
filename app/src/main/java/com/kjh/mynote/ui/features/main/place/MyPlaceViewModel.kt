package com.kjh.mynote.ui.features.main.place

import androidx.lifecycle.viewModelScope
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.CalendarUtils.getStartAndEndOfMonth
import com.kjh.mynote.utils.CalendarUtils.millisToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

@HiltViewModel
class MyPlaceViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _weekEvents = MutableStateFlow<Map<LocalDate, List<PlaceNoteModel>>>(emptyMap())
    val weekEvents = _weekEvents.asStateFlow()

    val placeItemsInDayFlow = combine(
        _selectedDate, _weekEvents
    ) { date, map ->
        map[date] ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun getPlacesInMonth(localDate: LocalDate? = null) {
        val (startOfMonth, endOfMonth) =
            if (localDate == null) {
                getStartAndEndOfMonth(_selectedDate.value.year, _selectedDate.value.monthValue)
            } else {
                getStartAndEndOfMonth(localDate.year, localDate.monthValue)
            }

        viewModelScope.launch {
            noteRepository.getPlacesInDateRange(
                startDate = startOfMonth,
                endDate = endOfMonth
            ).collect { result ->
                when (result) {
                    Result.Loading -> {}
                    is Result.Success -> {
                        val placesInRange = result.data ?: emptyList()

                        val weekNotesMap = placesInRange.groupBy { millisToLocalDate(it.visitDate) }
                        _weekEvents.value += weekNotesMap
                    }
                    is Result.Error -> {}
                }
            }
        }
    }

    fun checkHasNoteInDay(day: LocalDate): Boolean {
        return !_weekEvents.value[day].isNullOrEmpty()
    }

    fun changeSelectedDate(newDate: LocalDate) {
        if (newDate.year != _selectedDate.value.year ||
            newDate.monthValue != _selectedDate.value.monthValue) {
            getPlacesInMonth(newDate)
        }

        _selectedDate.value = newDate
    }

    fun getSelectedDate() = _selectedDate.value
}



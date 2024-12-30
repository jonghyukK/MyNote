package com.kjh.mynote.ui.features.place.home.weekview.dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 28..
 * Description:
 */

@HiltViewModel
class MonthlyEventCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val selectedDate: LocalDate =
        LocalDate.parse(savedStateHandle.get<String>(AppConstants.INTENT_DATE)) ?: LocalDate.now()

    val eventDays = savedStateHandle.get<ArrayList<String>>(AppConstants.INTENT_DATE_LIST)?.map {
        LocalDate.parse(it)
    } ?: emptyList()

    private val _currentMonth = MutableStateFlow(selectedDate)
    val currentMonth = _currentMonth.asStateFlow()

    private val _swipeMonthEvent = MutableSharedFlow<LocalDate>()
    val swipeMonthEvent = _swipeMonthEvent.asSharedFlow()

    fun swipeNextMonth() {
        viewModelScope.launch {
            val nextMonth = _currentMonth.value.plusMonths(1)
            _swipeMonthEvent.emit(nextMonth)

            changeMonth(nextMonth)
        }
    }

    fun swipePrevMonth() {
        viewModelScope.launch {
            val prevMonth = _currentMonth.value.minusMonths(1)
            _swipeMonthEvent.emit(prevMonth)

            changeMonth(prevMonth)
        }
    }

    fun changeMonth(newMonth: LocalDate) {
        _currentMonth.value = newMonth
    }
}
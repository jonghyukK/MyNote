package com.kjh.mynote.ui.features.place.calendar

import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

enum class DisplayType {
    WEEK_VIEW,
    LIST
}

@HiltViewModel
class PlaceNoteCalendarHomeViewModel @Inject constructor(): BaseViewModel() {

    private val _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDay = _selectedDay.asStateFlow()

    private val _displayType = MutableStateFlow(DisplayType.WEEK_VIEW)
    val displayType = _displayType.asStateFlow()

    fun changeViewType() {
        if (_displayType.value == DisplayType.WEEK_VIEW) {
            _displayType.value = DisplayType.LIST
        } else {
            _displayType.value = DisplayType.WEEK_VIEW
        }
    }

    fun selectDay(newDay: LocalDate) {
        _selectedDay.value = newDay
    }

    fun getSelectedDate() = _selectedDay.value
}



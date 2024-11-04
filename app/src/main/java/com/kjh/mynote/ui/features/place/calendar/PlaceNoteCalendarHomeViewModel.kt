package com.kjh.mynote.ui.features.place.calendar

import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

data class PlaceNoteCalendarHomeUiState(
    val selectedDay: LocalDate = LocalDate.now(),
    val displayType: DisplayType = DisplayType.WEEK_VIEW
)

enum class DisplayType {
    WEEK_VIEW,
    LIST
}

@HiltViewModel
class PlaceNoteCalendarHomeViewModel @Inject constructor(): BaseViewModel() {

    private val _uiState = MutableStateFlow(PlaceNoteCalendarHomeUiState())
    val uiState = _uiState.asStateFlow()

    fun changeViewType() {
        val displayType = if (_uiState.value.displayType == DisplayType.WEEK_VIEW) {
            DisplayType.LIST
        } else {
            DisplayType.WEEK_VIEW
        }

        _uiState.update {
            it.copy(displayType = displayType)
        }
    }

    fun selectDay(newDay: LocalDate) {
        _uiState.update {
            it.copy(selectedDay = newDay)
        }
    }

    fun getSelectedDate() = _uiState.value.selectedDay
}



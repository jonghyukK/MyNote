package com.kjh.mynote.ui.features.place.calendar.list

import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetPlaceNotesUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 2..
 * Description:
 */

data class ListTypeUiState2(
    val currentMonth: LocalDate = LocalDate.now(),
    val currentPagePos: Int = 0,
    val monthsWithInMonthPlaceNoteItems: List<MonthWithPlaceNotesItem> = emptyList()
)

data class MonthWithPlaceNotesItem(
    val monthDate: LocalDate,
    val placeNoteItems: List<ListTypeCalendarPlaceNoteUI> = emptyList()
)

sealed class ListTypeCalendarPlaceNoteUI {
    data class HeaderItem(val localDate: LocalDate) : ListTypeCalendarPlaceNoteUI()
    data class PlaceNoteItem(val item: PlaceNoteUiModel) : ListTypeCalendarPlaceNoteUI()
    object EmptyItem: ListTypeCalendarPlaceNoteUI()
}

@HiltViewModel
class PlaceNoteListTypeViewModel @Inject constructor(
    private val getPlaceNotesUseCase: GetPlaceNotesUseCase
): BaseViewModel() {

    private val _uiState = MutableStateFlow(ListTypeUiState2())
    val uiState = _uiState.asStateFlow()

    private val _targetDate = MutableStateFlow(LocalDate.now())

    private val _monthlyPlaceNoteFlow = getPlaceNotesUseCase()
        .map { notes ->
            val noteMap = notes.toUiModel().groupBy { it.localDate.withDayOfMonth(1) }
            generateCurrentToPastMonths(LocalDate.now()).map { month ->
                MonthWithPlaceNotesItem(
                    monthDate = month,
                    placeNoteItems = makeListViewTypePlaceNoteItems(noteMap[month] ?: emptyList())
                )
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            combine(_targetDate, _monthlyPlaceNoteFlow) { date, notes ->
                date to notes
            }.collect { (date, notes) ->
                _uiState.update {
                    it.copy(
                        currentMonth = date,
                        currentPagePos = notes.indexOfFirst { it.monthDate == date },
                        monthsWithInMonthPlaceNoteItems = notes
                    )
                }
            }
        }
    }

    fun setTargetDate(date: LocalDate) {
        if (date == _targetDate.value) return
        _targetDate.value = date
    }

    fun findDateUsingPagerPosition(pos: Int): LocalDate {
        return _uiState.value.monthsWithInMonthPlaceNoteItems[pos].monthDate
    }

    private fun generateCurrentToPastMonths(now: LocalDate): List<LocalDate> =
        (12 downTo 0).map {
            now.minusMonths(it.toLong()).withDayOfMonth(1)
        }

    private fun makeListViewTypePlaceNoteItems(items: List<PlaceNoteUiModel>): List<ListTypeCalendarPlaceNoteUI> {
        if (items.isEmpty()) {
            return listOf(ListTypeCalendarPlaceNoteUI.EmptyItem)
        }

        val uiItems = mutableListOf<ListTypeCalendarPlaceNoteUI>()
        var prevDate: LocalDate? = null

        for (item in items) {
            if (prevDate == null || item.localDate != prevDate) {
                uiItems.add(ListTypeCalendarPlaceNoteUI.HeaderItem(item.localDate))
            }

            uiItems.add(ListTypeCalendarPlaceNoteUI.PlaceNoteItem(item))
            prevDate = item.localDate
        }

        return uiItems
    }

}
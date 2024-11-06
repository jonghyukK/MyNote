package com.kjh.mynote.ui.features.place.calendar

import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetPlaceNotesUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
 * Created On 2024. 10. 11..
 * Description:
*/

enum class DisplayType {
    WEEK_VIEW,
    LIST
}

data class WeekViewUiState(
    val currentDay: LocalDate = LocalDate.now(),
    val selectedDayNoteItems: List<WeekViewTypeCalendarPlaceNoteUI> = emptyList(),
    val selectedMonthEventDays: List<LocalDate> = emptyList()
)

data class ListTypeUiState(
    val currentMonth: LocalDate = LocalDate.now(),
    val currentPagePos: Int = 0,
    val isLastPage: Boolean = false,
    val monthsWithInMonthPlaceNoteItems: List<MonthWithPlaceNotesItem> = emptyList()
)

@HiltViewModel
class PlaceNoteCalendarHomeViewModel @Inject constructor(
    private val getPlaceNotesUseCase: GetPlaceNotesUseCase
): BaseViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))

    private val _displayType = MutableStateFlow(DisplayType.WEEK_VIEW)
    val displayType = _displayType.asStateFlow()

    private val _weekViewUiState = MutableStateFlow(WeekViewUiState())
    val weekViewUiState = _weekViewUiState.asStateFlow()

    private val _listUiState = MutableStateFlow(ListTypeUiState())
    val listUiState = _listUiState.asStateFlow()

    val groupedNotesByLocalDateFlow = getPlaceNotesUseCase()
        .map { notes -> notes.toUiModel().groupBy { it.localDate } }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val groupedNotesByMonthDateFlow = getPlaceNotesUseCase()
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
        // weekView type..
        viewModelScope.launch {
            combine(_selectedDate, groupedNotesByLocalDateFlow) { date, noteMap ->
                val notesInDay = noteMap[date] ?: emptyList()
                val hasNoteDatesInMonth = noteMap.filter {
                    it.key.withDayOfMonth(1) == date.withDayOfMonth(1)
                }.keys

                WeekViewUiState(
                    currentDay = date,
                    selectedDayNoteItems = makeWeekViewTypePlaceNoteItems(notesInDay),
                    selectedMonthEventDays = hasNoteDatesInMonth.toList()
                )
            }.collectLatest {
                _weekViewUiState.value = it
            }
        }

        viewModelScope.launch {
            combine(_selectedMonth, groupedNotesByMonthDateFlow) { date, notes ->
                date to notes
            }.collect { (date, notes) ->
                val pagerPosition = notes.indexOfFirst { note -> note.monthDate == date }

                _listUiState.update {
                    it.copy(
                        currentMonth = date,
                        currentPagePos = pagerPosition,
                        isLastPage = pagerPosition == notes.size - 1,
                        monthsWithInMonthPlaceNoteItems = notes
                    )
                }
            }
        }
    }

    fun changeViewType() {
        if (_displayType.value == DisplayType.WEEK_VIEW) {
            _displayType.value = DisplayType.LIST
        } else {
            _displayType.value = DisplayType.WEEK_VIEW
            findFirstNoteDateAndSetInThisMonth()
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date

        if (_selectedMonth.value != date.withDayOfMonth(1)) {
            _selectedMonth.value = date.withDayOfMonth(1)
        }
    }

    fun getSelectedDate() = _selectedDate.value

    fun setSelectedMonth(pos: Int) {
        val item = groupedNotesByMonthDateFlow.value[pos].monthDate
        _selectedMonth.value = item
    }

    fun moveToPrevMonth() {
        val prevMonth = _listUiState.value.currentMonth.minusMonths(1)
        _listUiState.update {
            it.copy(
                currentMonth = prevMonth,
                currentPagePos = it.currentPagePos - 1,
                isLastPage = false
            )
        }
    }

    fun moveToNextMonth() {
        val nextMonth = _listUiState.value.currentMonth.plusMonths(1)
        _listUiState.update {
            it.copy(
                currentMonth = nextMonth,
                currentPagePos = it.currentPagePos + 1,
                isLastPage = hasNextPageWhenListType(it.currentPagePos + 1)
            )
        }
    }

    private fun hasNextPageWhenListType(pagePos: Int): Boolean {
        val monthItemSize = _listUiState.value.monthsWithInMonthPlaceNoteItems.size
        return pagePos < monthItemSize - 1
    }

    private fun findFirstNoteDateAndSetInThisMonth() {
        val item = groupedNotesByLocalDateFlow.value.keys.firstOrNull {
            it.withDayOfMonth(1) == _selectedMonth.value
        }

        _selectedDate.value = item ?: _selectedMonth.value.withDayOfMonth(15)
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

    data object EmptyItem: WeekViewTypeCalendarPlaceNoteUI()
}

data class MonthWithPlaceNotesItem(
    val monthDate: LocalDate,
    val placeNoteItems: List<ListTypeCalendarPlaceNoteUI> = emptyList()
)

sealed class ListTypeCalendarPlaceNoteUI {
    data class HeaderItem(val localDate: LocalDate) : ListTypeCalendarPlaceNoteUI()
    data class PlaceNoteItem(val item: PlaceNoteUiModel) : ListTypeCalendarPlaceNoteUI()
    data object EmptyItem: ListTypeCalendarPlaceNoteUI()
}



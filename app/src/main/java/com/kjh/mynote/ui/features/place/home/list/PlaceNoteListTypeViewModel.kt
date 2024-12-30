package com.kjh.mynote.ui.features.place.home.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.model.PlaceNoteUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 29..
 * Description:
 */

@HiltViewModel
class PlaceNoteListTypeViewModel @Inject constructor(): ViewModel() {

    private val _currentMonth = MutableStateFlow(LocalDate.now().withDayOfMonth(1))

    private val _groupedPlaceNotesByDate = MutableStateFlow<List<MonthWithPlaceNoteUiItem>>(emptyList())

    val uiState = combine(
        _currentMonth, _groupedPlaceNotesByDate
    ) { date, uiItemList ->
        val pagerPosition = uiItemList.indexOfFirst { it.month == date }

        PlaceNoteListTypeUiState(
            currentMonth = date,
            currentPagePos = pagerPosition,
            isLastPage = pagerPosition == uiItemList.size - 1,
            monthWithPlaceNoteUiItems = uiItemList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaceNoteListTypeUiState()
    )

    fun setCurrentDate(date: LocalDate) {
        if (_currentMonth.value == date.withDayOfMonth(1)) return

        _currentMonth.value = date.withDayOfMonth(1)
    }

    fun setPlaceNotes(list: List<PlaceNoteUiModel>) {
        val placeNotesGroupedByDate = list.groupBy { it.localDate.withDayOfMonth(1) }
        val pastYearMonths = makeCurrentToPastYearMonths(LocalDate.now())

        val uiItems = pastYearMonths.map { month ->
            MonthWithPlaceNoteUiItem(
                month = month,
                uiItems = makePlaceNoteUiItems(
                    placeNotesGroupedByDate[month] ?: emptyList()
                )
            )
        }

        _groupedPlaceNotesByDate.value = uiItems
    }

    private fun makeCurrentToPastYearMonths(now: LocalDate): List<LocalDate> =
        (12 downTo 0).map {
            now.minusMonths(it.toLong()).withDayOfMonth(1)
        }

    private fun makePlaceNoteUiItems(placeNotes: List<PlaceNoteUiModel>): List<PlaceNoteListTypeUiItem> {
        if (placeNotes.isEmpty()) {
            return listOf(PlaceNoteListTypeUiItem.EmptyItem)
        }

        val uiItems = mutableListOf<PlaceNoteListTypeUiItem>()
        var prevDate: LocalDate? = null

        for (placeNote in placeNotes) {
            if (prevDate == null || placeNote.localDate != prevDate) {
                uiItems.add(PlaceNoteListTypeUiItem.DateItem(placeNote.localDate))
            }

            uiItems.add(PlaceNoteListTypeUiItem.PlaceNoteItem(placeNote))
            prevDate = placeNote.localDate
        }

        return uiItems
    }
}

data class MonthWithPlaceNoteUiItem(
    val month: LocalDate,
    val uiItems: List<PlaceNoteListTypeUiItem>
)

sealed class PlaceNoteListTypeUiItem {
    data class DateItem(val localDate: LocalDate): PlaceNoteListTypeUiItem()
    data class PlaceNoteItem(val item: PlaceNoteUiModel): PlaceNoteListTypeUiItem()
    data object EmptyItem: PlaceNoteListTypeUiItem()
}

data class PlaceNoteListTypeUiState(
    val currentMonth: LocalDate = LocalDate.now(),
    val currentPagePos: Int = 0,
    val isLastPage: Boolean = false,
    val monthWithPlaceNoteUiItems: List<MonthWithPlaceNoteUiItem> = emptyList()
)
package com.kjh.mynote.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetPlaceNotesByDateRangeUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.getWeekStartAndEndDates
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

sealed class HomePlaceNoteUiState {
    data object Empty: HomePlaceNoteUiState()
    data object More: HomePlaceNoteUiState()
    data class PlaceNote(val item: PlaceNoteUiModel): HomePlaceNoteUiState()
}

sealed class HomeItem {
    data class HomePlaceNoteWeekView(
        val todayDate: LocalDate = LocalDate.now(),
        val placeNotesByDate: Map<LocalDate, List<PlaceNoteUiModel>> = emptyMap(),
        val selectedDate: LocalDate = todayDate,
        val displayedPlaceNotes: List<HomePlaceNoteUiState> = emptyList(),
        val eventDays: List<LocalDate> = emptyList()
    ): HomeItem()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPlaceNotesByDateRangeUseCase: GetPlaceNotesByDateRangeUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<List<HomeItem>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (startDate, endDate) = LocalDate.now().getWeekStartAndEndDates()

            getPlaceNotesByDateRangeUseCase(
                startDate = startDate.toMillis(),
                endDate = endDate.toMillis()
            )
                .map { notes -> notes.toUiModel().groupBy { it.localDate } }
                .collect { groupedPlaceNote ->
                    var homePlaceNoteWeekView = HomeItem.HomePlaceNoteWeekView()

                    val placeNotesInSelectedDay =
                        groupedPlaceNote[homePlaceNoteWeekView.selectedDate] ?: emptyList()

                    homePlaceNoteWeekView = homePlaceNoteWeekView.copy(
                        placeNotesByDate = groupedPlaceNote,
                        displayedPlaceNotes = makeHomePlaceNoteUiState(placeNotesInSelectedDay),
                        eventDays = groupedPlaceNote.keys.toList()
                    )

                    val index = _uiState.value.indexOfFirst { it is HomeItem.HomePlaceNoteWeekView }
                    if (index == -1) {
                        _uiState.value = listOf(homePlaceNoteWeekView)
                    } else {
                        _uiState.value = _uiState.value.map { state ->
                            if (state is HomeItem.HomePlaceNoteWeekView) {
                                homePlaceNoteWeekView
                            } else {
                                state
                            }
                        }
                    }
                }
        }
    }

    fun changePlaceNoteWeekDay(newDay: LocalDate) {
        _uiState.update { uiState ->
            uiState.map { uiList ->
                if (uiList is HomeItem.HomePlaceNoteWeekView) {
                    val placeNoteItemsInSelectedDay = uiList.placeNotesByDate[newDay] ?: emptyList()
                    uiList.copy(
                        selectedDate = newDay,
                        displayedPlaceNotes = makeHomePlaceNoteUiState(placeNoteItemsInSelectedDay)
                    )
                } else {
                    uiList
                }
            }
        }
    }

    private fun makeHomePlaceNoteUiState(
        placeNotes: List<PlaceNoteUiModel>
    ): List<HomePlaceNoteUiState> = when {
        placeNotes.isEmpty() -> {
            listOf(HomePlaceNoteUiState.Empty)
        }
        placeNotes.size > 3 -> {
            placeNotes
                .take(3)
                .map { HomePlaceNoteUiState.PlaceNote(it) } + listOf(HomePlaceNoteUiState.More)
        }
        else -> {
            placeNotes.map { HomePlaceNoteUiState.PlaceNote(it) }
        }
    }
}
package com.kjh.mynote.ui.features.place.home.weekview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.model.PlaceNoteUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 27..
 * Description:
 */

@HiltViewModel
class PlaceNoteWeekViewTypeViewModel @Inject constructor(): ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())

    private val _groupedPlaceNotesByDate = MutableStateFlow<Map<LocalDate, List<PlaceNoteUiModel>>>(emptyMap())

    val uiState: StateFlow<PlaceNoteWeekViewUiState> = combine(
        _currentDate, _groupedPlaceNotesByDate
    ) { date, placeNotesMap ->
        val selectedDayNotes = placeNotesMap[date] ?: emptyList()
        val selectedDayNoteUiItems = makePlaceNoteUiItems(selectedDayNotes)

        PlaceNoteWeekViewUiState(
            currentDate = date,
            eventDays = placeNotesMap.keys.toList(),
            selectedDayNoteUiItems = selectedDayNoteUiItems
        )
    }.stateIn(
       scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaceNoteWeekViewUiState()
    )

    fun setCurrentDate(date: LocalDate) {
        if (_currentDate.value == date) return

        _currentDate.value = date
    }

    fun setPlaceNotes(list: List<PlaceNoteUiModel>) {
        _groupedPlaceNotesByDate.value = list.groupBy { it.localDate }
    }

    private fun makePlaceNoteUiItems(placeNotes: List<PlaceNoteUiModel>): List<PlaceNoteWeekViewUiItem> {
        if (placeNotes.isEmpty()) {
            return listOf(PlaceNoteWeekViewUiItem.EmptyItem)
        }

        return placeNotes.map { note ->
            when (note.placeImages.size) {
                1 -> PlaceNoteWeekViewUiItem.OnePicturePlaceNoteItem(note)
                2 -> PlaceNoteWeekViewUiItem.TwoPicturePlaceNoteItem(note)
                3 -> PlaceNoteWeekViewUiItem.ThreePicturePlaceNoteItem(note)
                4 -> PlaceNoteWeekViewUiItem.FourPicturePlaceNoteItem(note)
                else -> PlaceNoteWeekViewUiItem.OverPicturePlaceNoteItem(
                    item = note,
                    remainImgCount = note.placeImages.size - 5
                )
            }
        }
    }
}

sealed class PlaceNoteWeekViewUiItem {
    data class OnePicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        PlaceNoteWeekViewUiItem()

    data class TwoPicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        PlaceNoteWeekViewUiItem()

    data class ThreePicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        PlaceNoteWeekViewUiItem()

    data class FourPicturePlaceNoteItem(val item: PlaceNoteUiModel) :
        PlaceNoteWeekViewUiItem()

    data class OverPicturePlaceNoteItem(
        val item: PlaceNoteUiModel,
        val remainImgCount: Int
    ) : PlaceNoteWeekViewUiItem()

    data object EmptyItem: PlaceNoteWeekViewUiItem()
}

data class PlaceNoteWeekViewUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val eventDays: List<LocalDate> = emptyList(),
    val selectedDayNoteUiItems: List<PlaceNoteWeekViewUiItem> = emptyList()
)
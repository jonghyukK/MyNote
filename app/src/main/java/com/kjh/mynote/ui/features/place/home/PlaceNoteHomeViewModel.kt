package com.kjh.mynote.ui.features.place.home

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.asResult
import com.example.domain.usecase.GetPlaceNotesUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
*/

@HiltViewModel
class PlaceNoteHomeViewModel @Inject constructor(
    private val getPlaceNotesUseCase: GetPlaceNotesUseCase
): BaseViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate = _currentDate.asStateFlow()

    private val _displayType = MutableStateFlow(DisplayType.WEEK_VIEW)
    val displayType = _displayType.asStateFlow()

    val placeNotesUiState = getPlaceNotesUseCase()
        .asResult()
        .map { result ->
            when (result) {
                is ApiResult.Loading -> PlaceNotesUiState.Loading
                is ApiResult.Error -> PlaceNotesUiState.Error(result.error)
                is ApiResult.Success -> PlaceNotesUiState.Success(result.data.toUiModel())
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PlaceNotesUiState.Loading
        )

    fun updateCurrentDate(newDate: LocalDate) {
        if (_currentDate.value == newDate) return

        _currentDate.value = newDate
    }

    fun changeViewType() {
        _displayType.value = when (_displayType.value) {
            DisplayType.WEEK_VIEW -> DisplayType.LIST
            DisplayType.LIST -> {
                getLatestNoteDay()
                DisplayType.WEEK_VIEW
            }
        }
    }

    private fun getLatestNoteDay() {
        val placeNotesUiState = placeNotesUiState.value as? PlaceNotesUiState.Success ?: return
        val eventDays = placeNotesUiState.placeNotes.groupBy { it.localDate }
        val existNoteLatestDay = eventDays.keys.toList().firstOrNull {
            _currentDate.value.withDayOfMonth(1) == it.withDayOfMonth(1)
        } ?: _currentDate.value

        _currentDate.value = existNoteLatestDay
    }
}

enum class DisplayType {
    WEEK_VIEW,
    LIST
}

sealed interface PlaceNotesUiState {
    data object Loading: PlaceNotesUiState
    data class Error(val error: Throwable): PlaceNotesUiState
    data class Success(val placeNotes: List<PlaceNoteUiModel>): PlaceNotesUiState
}


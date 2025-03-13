package com.kjh.mynote.ui_compose.feature.purchasenote

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui_compose.base.BaseComposeViewModel
import com.kjh.mynote.ui_compose.base.UiEvent
import com.kjh.mynote.ui_compose.base.UiSideEffect
import com.kjh.mynote.ui_compose.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 13..
 * Description:
 */

sealed class PurchaseNoteHomeUiEvent: UiEvent {
    data object LoadingPurchaseNotes: PurchaseNoteHomeUiEvent()
    data class LoadedPurchaseNotes(
        val groupedByDateNotesMap: Map<LocalDate, List<PurchaseNoteUiModel>>,
        val selectedDayPurchaseNotes: List<PurchaseNoteUiModel>,
        val hasNoteDays: List<LocalDate>
    ): PurchaseNoteHomeUiEvent()
    data class UpdateSelectedDay(val date: LocalDate): PurchaseNoteHomeUiEvent()
    data class UpdateCurrentMonth(val month: YearMonth): PurchaseNoteHomeUiEvent()
}

sealed class PurchaseNoteHomeSideEffect: UiSideEffect {
    data class ShowErrorToast(val message: String?): PurchaseNoteHomeSideEffect()
}

data class PurchaseNoteHomeUiState(
    val isLoading: Boolean = false,
    val currentMonth: YearMonth = YearMonth.now(),
    val groupedByDateNotesMap: Map<LocalDate, List<PurchaseNoteUiModel>> = emptyMap(),
    val hasNoteDays: List<LocalDate> = emptyList(),
    val selectedDay: LocalDate = LocalDate.now(),
    val selectedDayPurchaseNotes: List<PurchaseNoteUiModel> = emptyList()
): UiState

@HiltViewModel
class PurchaseNoteViewModel @Inject constructor(
    private val getFilteredPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase
): BaseComposeViewModel<PurchaseNoteHomeUiEvent, PurchaseNoteHomeUiState, PurchaseNoteHomeSideEffect>(
    { PurchaseNoteHomeUiState() }
) {

    init {
        fetchPurchaseNotes()
    }

    override fun reduceState(
        current: PurchaseNoteHomeUiState,
        event: PurchaseNoteHomeUiEvent,
    ): PurchaseNoteHomeUiState {
        return when (event) {
            is PurchaseNoteHomeUiEvent.LoadingPurchaseNotes -> {
                current.copy(isLoading = true)
            }
            is PurchaseNoteHomeUiEvent.LoadedPurchaseNotes -> {
                current.copy(
                    isLoading = false,
                    groupedByDateNotesMap = event.groupedByDateNotesMap,
                    selectedDayPurchaseNotes = event.selectedDayPurchaseNotes,
                    hasNoteDays = event.hasNoteDays
                )
            }
            is PurchaseNoteHomeUiEvent.UpdateSelectedDay -> {
                current.copy(
                    selectedDay = event.date,
                    selectedDayPurchaseNotes = current.groupedByDateNotesMap[event.date] ?: emptyList()
                )
            }
            is PurchaseNoteHomeUiEvent.UpdateCurrentMonth -> {
                current.copy(currentMonth = event.month)
            }
        }
    }

    override fun handleEvent(event: PurchaseNoteHomeUiEvent) {
//        when (event) {
//            PurchaseNoteHomeUiEvent.LoadingPurchaseNotes -> TODO()
//            is PurchaseNoteHomeUiEvent.LoadedPurchaseNotes -> TODO()
//            is PurchaseNoteHomeUiEvent.UpdateCurrentMonth -> TODO()
//            is PurchaseNoteHomeUiEvent.UpdateSelectedDay -> TODO()
//        }
        setEvent(event)
    }

    private fun fetchPurchaseNotes() {
        viewModelScope.launch {
            getFilteredPurchaseNotesUseCase().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PurchaseNoteHomeUiEvent.LoadingPurchaseNotes)
                    }
                    is ApiResult.Error -> {
                        setEffect(PurchaseNoteHomeSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        val purchaseNotes = result.data.map { it.toUiModel() }
                        val groupedByDateNoteMap = purchaseNotes.groupBy { it.localDate }
                        val selectedDay = state.value.selectedDay
                        val selectedDayPurchaseNotes =
                            groupedByDateNoteMap[selectedDay] ?: emptyList()
                        val hasNoteDays = groupedByDateNoteMap.keys.toList()

                        setEvent(
                            PurchaseNoteHomeUiEvent.LoadedPurchaseNotes(
                                groupedByDateNotesMap = groupedByDateNoteMap,
                                selectedDayPurchaseNotes = selectedDayPurchaseNotes,
                                hasNoteDays = hasNoteDays
                            )
                        )
                    }
                }
            }
        }
    }
}
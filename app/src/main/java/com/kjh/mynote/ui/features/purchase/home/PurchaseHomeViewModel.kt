package com.kjh.mynote.ui.features.purchase.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetPurchaseNotesUseCase
import com.kizitonwose.calendar.core.yearMonth
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

data class PurchaseNoteHomeUiState(
    val selectedDay: LocalDate = LocalDate.now(),
    val hasEventDays: List<LocalDate> = emptyList(),
    val selectedDayPurchaseNotes: List<PurchaseNoteUiModel> = emptyList()
)

@HiltViewModel
class PurchaseHomeViewModel @Inject constructor(
    private val getPurchaseNotesUseCase: GetPurchaseNotesUseCase
): ViewModel() {

    private val _currentMonth = MutableStateFlow(LocalDate.now().yearMonth)
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(LocalDate.now())

    private val purchaseNotesFlow = getPurchaseNotesUseCase()
        .map { it.toUiModel().groupBy { it.purchaseLocalDate } }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )

    val uiState: StateFlow<PurchaseNoteHomeUiState> = combine(
        purchaseNotesFlow, _selectedDay
    ) { notesMap, selectedDay ->
        PurchaseNoteHomeUiState(
            selectedDay = selectedDay,
            hasEventDays = notesMap.keys.toList(),
            selectedDayPurchaseNotes = notesMap[selectedDay] ?: emptyList()
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        PurchaseNoteHomeUiState()
    )

    fun setCurrentMonth(date: YearMonth) {
        _currentMonth.value = date
    }

    fun setSelectedDay(day: LocalDate) {
        _selectedDay.value = day
    }
}
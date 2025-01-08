package com.kjh.mynote.ui.features.purchase.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.kizitonwose.calendar.core.yearMonth
import com.kjh.mynote.model.Filters
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.getAppliedCategoryIds
import com.kjh.mynote.model.getAppliedPaymentMethodIds
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
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
    val selectedDayPurchaseNotes: List<PurchaseNoteUiModel> = emptyList(),
    val selectedDayTotalPrice: Long = 0
)

sealed interface PurchaseNotesUiState {
    data object Loading: PurchaseNotesUiState
    data class Error(val errorMsg: String): PurchaseNotesUiState
    data class Success(val groupedNotesMap: Map<LocalDate, List<PurchaseNoteUiModel>>): PurchaseNotesUiState
}
@HiltViewModel
class PurchaseHomeViewModel @Inject constructor(
    private val getFilteredPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase
): ViewModel() {

    private val _appliedFilterItems = MutableStateFlow<List<Filters>>(emptyList())
    val appliedFilterItems = _appliedFilterItems.asStateFlow()

    private val _currentMonth = MutableStateFlow(LocalDate.now().yearMonth)
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(LocalDate.now())

    private val purchaseNotesUiState = _appliedFilterItems.flatMapLatest { appliedFilterItems ->
        getFilteredPurchaseNotesUseCase(
            categoryIds = appliedFilterItems.getAppliedCategoryIds(),
            paymentMethodIds = appliedFilterItems.getAppliedPaymentMethodIds()
        )
            .map { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        PurchaseNotesUiState.Loading
                    }
                    is ApiResult.Error -> {
                        PurchaseNotesUiState.Error("구매노트 목록을 불러오는데 실패하였습니다.")
                    }
                    is ApiResult.Success -> {
                        val purchaseNotesMap = result.data.toUiModel().groupBy { it.localDate }
                        PurchaseNotesUiState.Success(purchaseNotesMap)
                    }
                }
            }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            PurchaseNotesUiState.Loading
        )

    val uiState: StateFlow<PurchaseNoteHomeUiState> = purchaseNotesUiState
        .map { it as? PurchaseNotesUiState.Success }
        .filterNotNull()
        .map { it.groupedNotesMap }
        .combine(_selectedDay) { notesMap, selectedDay ->
            PurchaseNoteHomeUiState(
                selectedDay = selectedDay,
                hasEventDays = notesMap.keys.toList(),
                selectedDayPurchaseNotes = notesMap[selectedDay] ?: emptyList(),
                selectedDayTotalPrice = notesMap[selectedDay]?.sumOf { it.purchasePrice } ?: 0
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

    fun setFilters(items: List<Filters>) {
        _appliedFilterItems.value = items
    }
}
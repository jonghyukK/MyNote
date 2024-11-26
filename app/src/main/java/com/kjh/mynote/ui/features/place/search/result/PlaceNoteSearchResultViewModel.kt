package com.kjh.mynote.ui.features.place.search.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.usecase.GetFilteredSearchPlaceNotesUseCase
import com.kjh.mynote.model.FilteredSearchPlaceNotesUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

sealed class DateRangeFilter {
    abstract fun getUiText(): String

    data class Monthly(val date: LocalDate): DateRangeFilter() {
        override fun getUiText(): String = date.toStringWithPattern("yyyy년 M월")
    }

    data class MonthOne(
        val startDate: LocalDate = LocalDate.now().minusMonths(1),
        val endDate: LocalDate = LocalDate.now()
    ): DateRangeFilter() {
        override fun getUiText(): String =
            "${startDate.toStringWithPattern("yyyy-MM-dd")} ~ ${endDate.toStringWithPattern("yyyy-MM-dd")}"
    }

    data class MonthThree(
        val startDate: LocalDate = LocalDate.now().minusMonths(3),
        val endDate: LocalDate = LocalDate.now()
    ): DateRangeFilter() {
        override fun getUiText(): String =
            "${startDate.toStringWithPattern("yyyy-MM-dd")} ~ ${endDate.toStringWithPattern("yyyy-MM-dd")}"
    }

    data class Directly(
        val startDate: LocalDate,
        val endDate: LocalDate
    ): DateRangeFilter() {
        fun getStartDateUiText() = startDate.toStringWithPattern("yyyy-MM-dd")
        fun getEndDateUiText() = endDate.toStringWithPattern("yyyy-MM-dd")

        override fun getUiText(): String =
            "${getStartDateUiText()} ~ ${getEndDateUiText()}"
    }
}

data class SearchPlaceNoteResultUiState(
    val isLoading: Boolean = true,
    val isError: String? = null,
    val monthFilter: DateRangeFilter? = null,
    val isDescending: Boolean = true,
    val resultItems: List<FilteredSearchPlaceNotesUiModel> = emptyList(),
    val resultTotalCount: Int = 0
)

fun SearchPlaceNoteResultUiState.hasAppliedFilters() = monthFilter != null

fun SearchPlaceNoteResultUiState.isEmpty() = !isLoading && resultItems.isEmpty()

@HiltViewModel
class SearchPlaceNoteResultViewModel @Inject constructor(
    private val getFilteredSearchPlaceNotesUseCase: GetFilteredSearchPlaceNotesUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val queryText = savedStateHandle.getStateFlow(AppConstants.INTENT_QUERY_TEXT, "")

    private val _uiState = MutableStateFlow(SearchPlaceNoteResultUiState())
    val uiState = _uiState.asStateFlow()

    fun getFilteredPlaceNotes() {
        val (startDate, endDate) = getStartDateAndEndDateTimeMillis(_uiState.value.monthFilter)

        viewModelScope.launch {
            getFilteredSearchPlaceNotesUseCase(
                query = queryText.value,
                startDate = startDate,
                endDate = endDate,
                isDescending = _uiState.value.isDescending
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                isError = null,
                                resultItems = emptyList(),
                                resultTotalCount = 0
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isError = result.msg ?: "장소노트 검색이 실패하였습니다.",
                                resultItems = emptyList(),
                                resultTotalCount = 0
                            )
                        }
                    }
                    is Result.Success -> {
                        delay(500)

                        val data = result.data ?: emptyList()
                        val totalNoteCount = data.sumOf { it.placeNotes.size }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                resultItems = data.toUiModel(),
                                resultTotalCount = totalNoteCount
                            )
                        }
                    }
                }
            }
        }
    }

    fun applyMonthFilter(filter: DateRangeFilter?) {
        if (_uiState.value.monthFilter == filter) return

        _uiState.update {
            it.copy(monthFilter = filter)
        }

        getFilteredPlaceNotes()
    }

    fun applySortFilter(isDescending: Boolean) {
        if (_uiState.value.isDescending == isDescending) return

        _uiState.update {
            it.copy(isDescending = isDescending)
        }

        getFilteredPlaceNotes()
    }

    fun shownError() {
        _uiState.update {
            it.copy(isError = null)
        }
    }

    private fun getStartDateAndEndDateTimeMillis(monthFilter: DateRangeFilter?): Pair<Long, Long> =
        when (monthFilter) {
            is DateRangeFilter.Monthly -> {
                Pair(
                    monthFilter.date.getFirstDayOfMonth().toMillis(),
                    monthFilter.date.getLastDayOfMonth().toMillis()
                )
            }
            is DateRangeFilter.MonthOne -> {
                Pair(
                    LocalDate.now().minusMonths(1).toMillis(),
                    LocalDate.now().toMillis()
                )
            }
            is DateRangeFilter.MonthThree -> {
                Pair(
                    LocalDate.now().minusMonths(3).toMillis(),
                    LocalDate.now().toMillis()
                )
            }
            is DateRangeFilter.Directly -> {
                Pair(
                    monthFilter.startDate.toMillis(),
                    monthFilter.endDate.toMillis()
                )
            }
            else -> {
                Pair(
                    LocalDate.now().minusYears(1).toMillis(),
                    LocalDate.now().toMillis()
                )
            }
        }

}
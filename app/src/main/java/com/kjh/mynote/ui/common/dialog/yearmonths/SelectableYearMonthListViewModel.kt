package com.kjh.mynote.ui.common.dialog.yearmonths

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog.Companion.ARG_INT_YEARS_RANGE
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthListBSDialog.Companion.ARG_LONG_DATE
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toLocalDate
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 19..
 * Description:
 */

data class SelectableYearMonthItem(
    val date: LocalDate,
    val isSelected: Boolean
)

@HiltViewModel
class SelectableYearMonthListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _selectedDate: StateFlow<Long> =
        savedStateHandle.getStateFlow(ARG_LONG_DATE, LocalDate.now().toMillis())

    private val _yearsRange: Int =
        savedStateHandle[ARG_INT_YEARS_RANGE] ?: AppConstants.DEFAULT_YEARS_RANGE

    val uiState: StateFlow<List<SelectableYearMonthItem>> =
        _selectedDate
            .map { dateLong -> generateUntilYearsAgo(dateLong.toLocalDate(), _yearsRange) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    /**
     * 현재 날짜를 기준으로 yearRange만큼 년월 리스트를 만듬.
     *
     * @param selectedDate
     * @param yearRange
     * @return List<SelectableYearMonthItem>
     */
    private fun generateUntilYearsAgo(
        selectedDate: LocalDate,
        yearRange: Int
    ): List<SelectableYearMonthItem> {
        val currentDate = LocalDate.now()
        val startDate = currentDate.minusYears(yearRange.toLong()).withDayOfMonth(1)

        val monthlyDates = mutableListOf<SelectableYearMonthItem>()

        var tempDate = startDate
        while (!tempDate.isAfter(currentDate)) {
            val isSelected = tempDate == selectedDate.withDayOfMonth(1)
            monthlyDates.add(SelectableYearMonthItem(date = tempDate, isSelected = isSelected))

            tempDate = tempDate.plusMonths(1)
        }

        return monthlyDates.reversed()
    }
}
package com.kjh.mynote.ui.features.purchase.search.filters.date

import androidx.lifecycle.ViewModel
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.purchase.search.Filters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 25..
 * Description:
 */

data class PurchaseNoteDatePeriodFilterUiState(
    val initMonthFilter: Filters.DateRange = Filters.DateRange(),
    val tempMonthFilter: Filters.DateRange = initMonthFilter
)

fun PurchaseNoteDatePeriodFilterUiState.isChangedFilter() =
    initMonthFilter.dateRangeFilter != tempMonthFilter.dateRangeFilter

@HiltViewModel
class PurchaseNoteDatePeriodFilterViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseNoteDatePeriodFilterUiState())
    val uiState = _uiState.asStateFlow()

    fun setInitFilter(filter: Filters.DateRange) {
        _uiState.value = PurchaseNoteDatePeriodFilterUiState(initMonthFilter = filter)
    }

    fun setTempMonthFilter(filter: DateRangeFilter) {
        _uiState.update {
            it.copy(
                tempMonthFilter = it.tempMonthFilter.copy(
                    dateRangeFilter = filter,
                    isApplied = true
                )
            )
        }
    }

    fun resetTempMonthFilter() {
        _uiState.update {
            it.copy(tempMonthFilter = it.tempMonthFilter.copy(
                dateRangeFilter = null,
                isApplied = false
            ))
        }
    }
}
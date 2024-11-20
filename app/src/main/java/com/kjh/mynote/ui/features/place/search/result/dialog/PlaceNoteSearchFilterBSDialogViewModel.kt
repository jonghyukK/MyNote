package com.kjh.mynote.ui.features.place.search.result.dialog

import androidx.lifecycle.ViewModel
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

data class PlaceNoteSearchFilterBSDialogUiState(
    val initMonthFilter: DateRangeFilter? = null,
    val tempMonthFilter: DateRangeFilter? = initMonthFilter
)

fun PlaceNoteSearchFilterBSDialogUiState.isChangedFilter() =
    initMonthFilter != tempMonthFilter

@HiltViewModel
class PlaceNoteSearchFilterBSDialogViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(PlaceNoteSearchFilterBSDialogUiState())
    val uiState = _uiState.asStateFlow()

    fun setInitFilter(filter: DateRangeFilter?) {
        _uiState.value = PlaceNoteSearchFilterBSDialogUiState(initMonthFilter = filter)
    }

    fun setTempMonthFilter(filter: DateRangeFilter) {
        _uiState.update {
            it.copy(tempMonthFilter = filter)
        }
    }

    fun resetTempMonthFilter() {
        _uiState.update {
            it.copy(tempMonthFilter = null)
        }
    }
}
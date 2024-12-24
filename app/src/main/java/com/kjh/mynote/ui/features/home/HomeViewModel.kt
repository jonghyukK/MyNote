package com.kjh.mynote.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryStats
import com.example.domain.model.asResult
import com.example.domain.usecase.GetPlaceNotesByDateRangeUseCase
import com.example.domain.usecase.GetPurchaseNoteStatisticsUseCase
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.R
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.getWeekStartAndEndDates
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

sealed interface WeeklyPlaceNotesUiState {
    data object Loading: WeeklyPlaceNotesUiState
    data class Error(val error: Throwable): WeeklyPlaceNotesUiState
    data class Success(val data: HomeUiItem.HomeWeeklyPurchaseNoteItem): WeeklyPlaceNotesUiState
}

sealed interface MonthlyPurchaseStatisticsUiState {
    data object Loading: MonthlyPurchaseStatisticsUiState
    data class Error(val error: Throwable): MonthlyPurchaseStatisticsUiState
    data class Success(val data: HomeUiItem.HomeMonthlyPurchaseStatisticsItem) : MonthlyPurchaseStatisticsUiState
}

sealed class WeeklyPlaceNoteUiItem {
    data object Empty: WeeklyPlaceNoteUiItem()
    data object More: WeeklyPlaceNoteUiItem()
    data class PlaceNote(val item: PlaceNoteUiModel): WeeklyPlaceNoteUiItem()
}

sealed class MonthlyCategoryStatsUiItem {
    data object More: MonthlyCategoryStatsUiItem()
    data class CategoryStatsItem(
        val item: CategoryStats,
        val color: Int
    ): MonthlyCategoryStatsUiItem()
}

sealed class HomeUiItem {
    data class HomeWeeklyPurchaseNoteItem(
        val selectedDate: LocalDate,
        val groupedNoteMap: Map<LocalDate, List<PlaceNoteUiModel>>,
        val placeNoteUiItems: List<WeeklyPlaceNoteUiItem>,
        val eventExistingDays: List<LocalDate>
    ): HomeUiItem()

    data class HomeMonthlyPurchaseStatisticsItem(
        val pieEntries: List<PieEntry>,
        val pieColors: List<Int>,
        val highlightedPieEntry: PieEntry? = null,
        val categoryStatsUiItems: List<MonthlyCategoryStatsUiItem>
    ): HomeUiItem()
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val uiItems: List<HomeUiItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPlaceNotesByDateRangeUseCase: GetPlaceNotesByDateRangeUseCase,
    private val getPurchaseNoteStatisticsUseCase: GetPurchaseNoteStatisticsUseCase
): ViewModel() {

    private val _todayDate: LocalDate = LocalDate.now()
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun getHomeData() {
        viewModelScope.launch {
            combine(
                weeklyPlaceNotesUiState,
                monthlyPurchaseStatisticsUiState
            ) { weeklyPlaceNotesUiState, monthlyPurchaseStatisticsUiState ->
                mergeHomeUiState(weeklyPlaceNotesUiState, monthlyPurchaseStatisticsUiState)
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun updateHighlightPieEntry(entry: PieEntry?) {
        _uiState.update { uiState ->
            uiState.copy(
                uiItems = uiState.uiItems.map { uiItem ->
                    if (uiItem is HomeUiItem.HomeMonthlyPurchaseStatisticsItem) {
                        uiItem.copy(highlightedPieEntry = entry)
                    } else {
                        uiItem
                    }
                }
            )
        }
    }

    fun updatePlaceNoteWeekDay(newDay: LocalDate) {
        _selectedDate.value = newDay

        _uiState.update { uiState ->
            uiState.copy(
                uiItems = uiState.uiItems.map { uiItem ->
                    if (uiItem is HomeUiItem.HomeWeeklyPurchaseNoteItem) {
                        val placeNoteItemsInSelectedDay = uiItem.groupedNoteMap[newDay] ?: emptyList()
                        uiItem.copy(
                            selectedDate = newDay,
                            placeNoteUiItems = makeHomePlaceNoteUiItems(placeNoteItemsInSelectedDay)
                        )
                    } else {
                        uiItem
                    }
                }
            )
        }
    }

    fun shownError() {
        _uiState.update {
            it.copy(
                errorMsg = null
            )
        }
    }

    private val weeklyPlaceNotesUiState: StateFlow<WeeklyPlaceNotesUiState> =
        weeklyPlaceNotesUiState(
            startDate = _todayDate.getWeekStartAndEndDates().first.toMillis(),
            endDate = _todayDate.getWeekStartAndEndDates().second.toMillis(),
            selectedDate = _selectedDate.value
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = WeeklyPlaceNotesUiState.Loading
            )

    private val monthlyPurchaseStatisticsUiState: StateFlow<MonthlyPurchaseStatisticsUiState> =
        monthlyPurchaseStatisticsUiState(
            startDate = _todayDate.getFirstDayOfMonth().toMillis(),
            endDate = _todayDate.getLastDayOfMonth().toMillis()
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = MonthlyPurchaseStatisticsUiState.Loading
            )

    private fun mergeHomeUiState(
        weeklyPurchaseNoteState: WeeklyPlaceNotesUiState,
        monthlyPurchaseStatisticsUiState: MonthlyPurchaseStatisticsUiState,
    ): HomeUiState {
        return when {
            weeklyPurchaseNoteState is WeeklyPlaceNotesUiState.Loading ||
                    monthlyPurchaseStatisticsUiState is MonthlyPurchaseStatisticsUiState.Loading -> {
                HomeUiState(
                    isLoading = true,
                    errorMsg = null,
                    uiItems = emptyList()
                )
            }

            weeklyPurchaseNoteState is WeeklyPlaceNotesUiState.Error -> {
                HomeUiState(
                    isLoading = false,
                    errorMsg = weeklyPurchaseNoteState.error.message
                        ?: "주간 구매노트 목록을 불러오는데 실패하였습니다.",
                    uiItems = emptyList()
                )
            }

            monthlyPurchaseStatisticsUiState is MonthlyPurchaseStatisticsUiState.Error -> {
                HomeUiState(
                    isLoading = false,
                    errorMsg = monthlyPurchaseStatisticsUiState.error.message
                        ?: "월간 구매노트 통계 데이터를 불러오는데 실패하였습니다.",
                    uiItems = emptyList()
                )
            }

            monthlyPurchaseStatisticsUiState is MonthlyPurchaseStatisticsUiState.Success &&
                    weeklyPurchaseNoteState is WeeklyPlaceNotesUiState.Success -> {
                HomeUiState(
                    isLoading = false,
                    errorMsg = null,
                    uiItems = listOf(
                        weeklyPurchaseNoteState.data,
                        monthlyPurchaseStatisticsUiState.data
                    )
                )
            }

            else -> HomeUiState(
                isLoading = false, errorMsg = null, uiItems = emptyList()
            )
        }
    }

    private fun weeklyPlaceNotesUiState(
        startDate: Long,
        endDate: Long,
        selectedDate: LocalDate,
    ): Flow<WeeklyPlaceNotesUiState> {
        return getPlaceNotesByDateRangeUseCase(startDate, endDate)
            .asResult()
            .map { placeNotesResult ->
                when (placeNotesResult) {
                    is ApiResult.Loading -> {
                        WeeklyPlaceNotesUiState.Loading
                    }
                    is ApiResult.Error -> {
                        WeeklyPlaceNotesUiState.Error(placeNotesResult.error)
                    }
                    is ApiResult.Success -> {
                        val groupedNoteMap = placeNotesResult.data.toUiModel()
                            .groupBy { it.localDate }

                        val noteInSelectedDay = groupedNoteMap[selectedDate] ?: emptyList()

                        WeeklyPlaceNotesUiState.Success(
                            data = HomeUiItem.HomeWeeklyPurchaseNoteItem(
                                selectedDate = selectedDate,
                                groupedNoteMap = groupedNoteMap,
                                placeNoteUiItems = makeHomePlaceNoteUiItems(noteInSelectedDay),
                                eventExistingDays = groupedNoteMap.keys.toList()
                            )
                        )
                    }
                }
            }
    }

    private fun monthlyPurchaseStatisticsUiState(
        startDate: Long,
        endDate: Long
    ): Flow<MonthlyPurchaseStatisticsUiState> {
        return getPurchaseNoteStatisticsUseCase(startDate, endDate)
            .asResult()
            .map { purchaseNoteStatisticsResult ->
                when (purchaseNoteStatisticsResult) {
                    is ApiResult.Loading -> {
                        MonthlyPurchaseStatisticsUiState.Loading
                    }
                    is ApiResult.Error -> {
                        MonthlyPurchaseStatisticsUiState.Error(purchaseNoteStatisticsResult.error)
                    }
                    is ApiResult.Success -> {
                        val top5Items = purchaseNoteStatisticsResult.data.categoryStatsList.take(5)
                        val pieColors = makePieColors(top5Items.size)

                        val categoryStatsUiItems: MutableList<MonthlyCategoryStatsUiItem> =
                            top5Items.mapIndexed { index, categoryStats ->
                                MonthlyCategoryStatsUiItem.CategoryStatsItem(
                                    item = categoryStats,
                                    color = pieColors[index]
                                )
                            }.toMutableList()

                        if (purchaseNoteStatisticsResult.data.categoryStatsList.size > 5) {
                            categoryStatsUiItems.add(MonthlyCategoryStatsUiItem.More)
                        }

                        MonthlyPurchaseStatisticsUiState.Success(
                            data = HomeUiItem.HomeMonthlyPurchaseStatisticsItem(
                                pieEntries = makePieEntry(top5Items),
                                pieColors = pieColors,
                                categoryStatsUiItems = categoryStatsUiItems
                            )
                        )
                    }
                }
            }
    }

    private fun makeHomePlaceNoteUiItems(
        placeNotes: List<PlaceNoteUiModel>
    ): List<WeeklyPlaceNoteUiItem> = when {
        placeNotes.isEmpty() -> {
            listOf(WeeklyPlaceNoteUiItem.Empty)
        }
        placeNotes.size > 3 -> {
            placeNotes
                .take(3)
                .map { WeeklyPlaceNoteUiItem.PlaceNote(it) } + listOf(WeeklyPlaceNoteUiItem.More)
        }
        else -> {
            placeNotes.map { WeeklyPlaceNoteUiItem.PlaceNote(it) }
        }
    }

    private fun makePieEntry(categoryWithPurchaseNotesStatsItem: List<CategoryStats>): List<PieEntry> {
        if (categoryWithPurchaseNotesStatsItem.isEmpty()) {
            return listOf(PieEntry(1f, "없음"))
        }

        return categoryWithPurchaseNotesStatsItem.map { data ->
            PieEntry(data.purchaseNoteTotalCount.toFloat(), data.categoryName)
        }
    }

    private fun makePieColors(entrySize: Int): List<Int> {
        if (entrySize == 0) {
            return listOf(R.color.black_400)
        }

        return (0..< entrySize).mapIndexed { index, _ ->
            AppConstants.chartColorList[index]
        }
    }
}
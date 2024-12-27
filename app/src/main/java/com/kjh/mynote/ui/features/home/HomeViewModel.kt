package com.kjh.mynote.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryStats
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPlaceNotesByDateRangeUseCase: GetPlaceNotesByDateRangeUseCase,
    private val getPurchaseNoteStatisticsUseCase: GetPurchaseNoteStatisticsUseCase
): ViewModel() {

    private val _todayDate: LocalDate = LocalDate.now()
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
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
        val uiState = _uiState.value as? HomeUiState.HomeUi ?: return

        val updatedUiItems = uiState.uiItems.map { uiItem ->
            when(uiItem) {
                is HomeUiItem.HomeMonthlyPurchaseStatisticsItem -> uiItem.copy(highlightedPieEntry = entry)
                else -> uiItem
            }
        }

        _uiState.value = uiState.copy(uiItems = updatedUiItems)
    }

    fun updatePlaceNoteWeekDay(newDay: LocalDate) {
        _selectedDate.value = newDay
    }

    private val weeklyPlaceNotesUiState: StateFlow<WeeklyPlaceNotesUiState> =
        weeklyPlaceNotesUiState(
            startDate = _todayDate.getWeekStartAndEndDates().first.toMillis(),
            endDate = _todayDate.getWeekStartAndEndDates().second.toMillis(),
            selectedDate = _selectedDate
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
                HomeUiState.Loading
            }

            weeklyPurchaseNoteState is WeeklyPlaceNotesUiState.Error -> {
                HomeUiState.Error(weeklyPurchaseNoteState.error)
            }

            monthlyPurchaseStatisticsUiState is MonthlyPurchaseStatisticsUiState.Error -> {
                HomeUiState.Error(monthlyPurchaseStatisticsUiState.error)
            }

            monthlyPurchaseStatisticsUiState is MonthlyPurchaseStatisticsUiState.Success &&
                    weeklyPurchaseNoteState is WeeklyPlaceNotesUiState.Success -> {
                val uiItems = listOf(
                    weeklyPurchaseNoteState.data,
                    monthlyPurchaseStatisticsUiState.data
                )

                HomeUiState.HomeUi(uiItems)
            }

            else -> HomeUiState.HomeUi(emptyList())
        }
    }


    private fun weeklyPlaceNotesUiState(
        startDate: Long,
        endDate: Long,
        selectedDate: StateFlow<LocalDate>,
    ): Flow<WeeklyPlaceNotesUiState> {
        return combine(
            selectedDate,
            getPlaceNotesByDateRangeUseCase(startDate, endDate)
        ) { date, placeNotesResult ->
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

                    val noteInSelectedDay = groupedNoteMap[date] ?: emptyList()

                    WeeklyPlaceNotesUiState.Success(
                        data = HomeUiItem.HomeWeeklyPurchaseNoteItem(
                            selectedDate = date,
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
            .map { purchaseNoteStatisticsResult ->
                when (purchaseNoteStatisticsResult) {
                    is ApiResult.Loading -> {
                        MonthlyPurchaseStatisticsUiState.Loading
                    }
                    is ApiResult.Error -> {
                        MonthlyPurchaseStatisticsUiState.Error(purchaseNoteStatisticsResult.error)
                    }
                    is ApiResult.Success -> {
                        val purchaseNoteStats = purchaseNoteStatisticsResult.data
                        val top5Items = purchaseNoteStats.categoryStatsList.take(5)
                        val pieColors = makePieColors(top5Items.size)

                        val categoryStatsUiItems: MutableList<MonthlyCategoryStatsUiItem> =
                            top5Items.mapIndexed { index, categoryStats ->
                                MonthlyCategoryStatsUiItem.CategoryStatsItem(
                                    item = categoryStats,
                                    color = pieColors[index]
                                )
                            }.toMutableList()

                        if (purchaseNoteStats.categoryStatsList.size > 5) {
                            categoryStatsUiItems.add(MonthlyCategoryStatsUiItem.More)
                        }

                        MonthlyPurchaseStatisticsUiState.Success(
                            data = HomeUiItem.HomeMonthlyPurchaseStatisticsItem(
                                totalNoteCount = purchaseNoteStats.totalNoteCount,
                                totalNotePrice = purchaseNoteStats.totalPurchasePrice,
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
        val totalNoteCount: Int,
        val totalNotePrice: Long,
        val pieEntries: List<PieEntry>,
        val pieColors: List<Int>,
        val highlightedPieEntry: PieEntry? = null,
        val categoryStatsUiItems: List<MonthlyCategoryStatsUiItem>
    ): HomeUiItem()
}

sealed interface HomeUiState {
    data object Loading: HomeUiState
    data class Error(val error: Throwable): HomeUiState
    data class HomeUi(val uiItems: List<HomeUiItem>): HomeUiState
}
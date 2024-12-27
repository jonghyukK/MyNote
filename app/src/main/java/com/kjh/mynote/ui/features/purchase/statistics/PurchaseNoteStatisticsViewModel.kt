package com.kjh.mynote.ui.features.purchase.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryStats
import com.example.domain.model.PurchaseNoteStatistics
import com.example.domain.usecase.GetPurchaseNoteStatisticsUseCase
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.R
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

@HiltViewModel
class PurchaseNoteStatisticsViewModel @Inject constructor(
    private val getPurchaseNoteStatisticsUseCase: GetPurchaseNoteStatisticsUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _currentDate = MutableStateFlow(savedStateHandle[AppConstants.INTENT_DATE] ?: LocalDate.now())
    val currentDate = _currentDate.asStateFlow()

    private val _uiState = MutableStateFlow<PurchaseNoteStatisticsUiState>(PurchaseNoteStatisticsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getPurchaseNoteStatistics() {
        viewModelScope.launch {
            currentDate.flatMapLatest { date ->
                getPurchaseNoteStatisticsUseCase(
                    startDate = date.getFirstDayOfMonth().toMillis(),
                    endDate =  date.getLastDayOfMonth().toMillis()
                ).map { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            PurchaseNoteStatisticsUiState.Loading
                        }
                        is ApiResult.Error -> {
                            PurchaseNoteStatisticsUiState.Error(result.error)
                        }
                        is ApiResult.Success -> {
                            val categoryStatsList = result.data.categoryStatsList

                            val statsInfoUiItem = makeStatsInfoUiItem(date, result.data)
                            val pieChartUiItem = makePieChartUiItem(categoryStatsList)

                            val uiItems = if (categoryStatsList.isEmpty()) {
                                listOf(statsInfoUiItem, pieChartUiItem) + PurchaseNoteStaticsUiItem.Empty
                            } else {
                                val categoryStatsUiItems = makeCategoryStatsUiItems(categoryStatsList)
                                listOf(statsInfoUiItem, pieChartUiItem) + categoryStatsUiItems
                            }

                            PurchaseNoteStatisticsUiState.Success(uiItems)
                        }
                    }
                }
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun setDate(newDate: LocalDate) {
        if (newDate == _currentDate.value) return

        _currentDate.value = newDate
    }

    fun updateHighlightEntry(pieEntry: PieEntry?) {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return

        val updatedUiItems = uiState.uiItems.map { uiItem ->
            when (uiItem) {
                is PurchaseNoteStaticsUiItem.PieChartItem -> uiItem.copy(highlightedPieEntry = pieEntry)
                else -> uiItem
            }
        }

        _uiState.value = uiState.copy(uiItems = updatedUiItems)
    }

    private fun makeStatsInfoUiItem(
        currentDate: LocalDate,
        data: PurchaseNoteStatistics,
    ): PurchaseNoteStaticsUiItem.StatsInfoItem = PurchaseNoteStaticsUiItem.StatsInfoItem(
        purchaseNoteTotalCount = data.totalNoteCount,
        purchaseNoteTotalPrice = data.totalPurchasePrice,
        currentDate = currentDate
    )

    private fun makePieChartUiItem(categoryStatsList: List<CategoryStats>) =
        PurchaseNoteStaticsUiItem.PieChartItem(
            isEmpty = categoryStatsList.isEmpty(),
            pieEntries = makePieEntry(categoryStatsList),
            pieColors = makePieColors(categoryStatsList.take(6).size)
        )

    private fun makeCategoryStatsUiItems(categoryStatsList: List<CategoryStats>) =
        categoryStatsList.mapIndexed { index, data ->
            PurchaseNoteStaticsUiItem.CategoryStatsItem(
                categoryStatsItem = data,
                color = AppConstants.chartColorList.getOrElse(index) {
                    AppConstants.chartColorList.last()
                }
            )
        }

    private fun makePieEntry(items: List<CategoryStats>): List<PieEntry> = when {
        items.isEmpty() -> {
            listOf(PieEntry(1f, "없음"))
        }
        items.size <= 5 -> {
            items.map { data ->
                PieEntry(data.purchaseNoteTotalCount.toFloat(), data.categoryName)
            }
        }
        else -> {
            val pieEntries: MutableList<PieEntry> = items.subList(0, 5).map { data ->
                PieEntry(data.purchaseNoteTotalCount.toFloat(), data.categoryName)
            }.toMutableList()

            val etcTotalCount = items.subList(5, items.size).sumOf { it.purchaseNoteTotalCount }
            pieEntries.add(PieEntry(etcTotalCount.toFloat(), "그 외"))

            pieEntries
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

sealed class PurchaseNoteStaticsUiItem {
    data object Empty: PurchaseNoteStaticsUiItem()

    data class StatsInfoItem(
        val purchaseNoteTotalCount: Int,
        val purchaseNoteTotalPrice: Long,
        val currentDate: LocalDate
    ): PurchaseNoteStaticsUiItem()

    data class PieChartItem(
        val isEmpty: Boolean = true,
        val pieEntries: List<PieEntry> = emptyList(),
        val pieColors: List<Int> = emptyList(),
        val highlightedPieEntry: PieEntry? = null
    ): PurchaseNoteStaticsUiItem()

    data class CategoryStatsItem(
        val categoryStatsItem: CategoryStats,
        val color: Int
    ): PurchaseNoteStaticsUiItem()
}

sealed interface PurchaseNoteStatisticsUiState {
    data object Loading: PurchaseNoteStatisticsUiState
    data class Error(val error: Throwable): PurchaseNoteStatisticsUiState
    data class Success(val uiItems: List<PurchaseNoteStaticsUiItem>): PurchaseNoteStatisticsUiState
}
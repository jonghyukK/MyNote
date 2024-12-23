package com.kjh.mynote.ui.features.purchase.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryStats
import com.example.domain.model.PurchaseNoteStatistics
import com.example.domain.model.asResult
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

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

data class PurchaseNoteStatisticsUiState(
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val currentDate: LocalDate = LocalDate.now(),
    val uiItems: List<PurchaseNoteStaticsUiItem> = emptyList()
)

@HiltViewModel
class PurchaseNoteStatisticsViewModel @Inject constructor(
    private val getPurchaseNoteStatisticsUseCase: GetPurchaseNoteStatisticsUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _initDate = savedStateHandle[AppConstants.INTENT_DATE] ?: LocalDate.now()

    private val _uiState = MutableStateFlow(PurchaseNoteStatisticsUiState(currentDate = _initDate))
    val uiState = _uiState.asStateFlow()

    fun fetchPurchaseNoteStatistics() {
        viewModelScope.launch {
            val currentDate = _uiState.value.currentDate
            val startDate = currentDate.getFirstDayOfMonth().toMillis()
            val endDate = currentDate.getLastDayOfMonth().toMillis()

            getPurchaseNoteStatisticsUseCase(startDate, endDate)
                .asResult()
                .collect { purchaseNoteStatisticsResult ->
                    when (purchaseNoteStatisticsResult) {
                        is ApiResult.Loading -> {
                            _uiState.update {
                                it.copy(isLoading = true)
                            }
                        }
                        is ApiResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMsg = purchaseNoteStatisticsResult.error.message ?: "구매노트 통계 데이터를 가져오는데 실패하였습니다.",
                                    uiItems = emptyList()
                                )
                            }
                        }
                        is ApiResult.Success -> {
                            val categoryStatsList = purchaseNoteStatisticsResult.data.categoryStatsList

                            val statsInfoUiItem = makeStatsInfoUiItem(currentDate, purchaseNoteStatisticsResult.data)
                            val pieChartUiItem = makePieChartUiItem(categoryStatsList)

                            val uiItems = if (categoryStatsList.isEmpty()) {
                                listOf(statsInfoUiItem, pieChartUiItem) + PurchaseNoteStaticsUiItem.Empty
                            } else {
                                val categoryStatsUiItems = makeCategoryStatsUiItems(categoryStatsList)

                                listOf(statsInfoUiItem, pieChartUiItem) + categoryStatsUiItems
                            }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    uiItems = uiItems
                                )
                            }
                        }
                    }
                }
        }
    }

    fun setDate(newDate: LocalDate) {
        if (newDate == _uiState.value.currentDate) return

        _uiState.update {
            it.copy(currentDate = newDate)
        }

        fetchPurchaseNoteStatistics()
    }

    fun updateHighlightEntry(pieEntry: PieEntry?) {
        _uiState.update { uiState ->
            uiState.copy(
                uiItems = uiState.uiItems.map { uiItem ->
                    if (uiItem is PurchaseNoteStaticsUiItem.PieChartItem) {
                        uiItem.copy(highlightedPieEntry = pieEntry)
                    } else {
                        uiItem
                    }
                }
            )
        }
    }

    fun shownError() {
        _uiState.update {
            it.copy(errorMsg = null)
        }
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
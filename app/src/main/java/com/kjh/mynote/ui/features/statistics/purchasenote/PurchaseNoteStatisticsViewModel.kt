package com.kjh.mynote.ui.features.statistics.purchasenote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryStats
import com.example.domain.model.PaymentMethodStats
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

    private val _uiState = MutableStateFlow<PurchaseNoteStatisticsUiState>(
        PurchaseNoteStatisticsUiState.Loading
    )
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
                            val paymentMethodStatsList = result.data.paymentMethodStatsList

                            val statsTotalSectionItem = makeStatsTotalSectionItem(date, result.data)
                            val categoryStatsSectionItem = makeCategoryStatsSectionItem(categoryStatsList)
                            val paymentMethodStatsSectionItem = makePaymentMethodStatsSectionItem(paymentMethodStatsList)

                            PurchaseNoteStatisticsUiState.Success(
                                listOf(
                                    statsTotalSectionItem,
                                    categoryStatsSectionItem,
                                    paymentMethodStatsSectionItem
                                )
                            )
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

    fun toggleExpandForCategoryStats() {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updatedItems = uiState.uiItems.map { currentUiItem ->
            if (currentUiItem is PurchaseNoteStatisticsUiItem.CategoryStatsSection) {
                currentUiItem.copy(
                    isExpanded = true
                )
            } else {
                currentUiItem
            }
        }

        _uiState.value = uiState.copy(uiItems = updatedItems)
    }

    fun toggleExpandForPaymentMethodStats() {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updatedItems = uiState.uiItems.map { currentUiItem ->
            if (currentUiItem is PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection) {
                currentUiItem.copy(
                    isExpanded = true
                )
            } else {
                currentUiItem
            }
        }

        _uiState.value = uiState.copy(uiItems = updatedItems)
    }

    fun updateHighlightForCategoryPie(pieEntry: PieEntry?) {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updateItems = uiState.uiItems.map { currentUiItem ->
            if (currentUiItem is PurchaseNoteStatisticsUiItem.CategoryStatsSection) {
                currentUiItem.copy(
                    pieChartItem = currentUiItem.pieChartItem.copy(
                        highlightedPieEntry = pieEntry
                    )
                )
            } else {
                currentUiItem
            }
        }

        _uiState.value = uiState.copy(uiItems = updateItems)
    }

    fun updateHighlightForPaymentMethodPie(pieEntry: PieEntry?) {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updateItems = uiState.uiItems.map { currentUiItem ->
            if (currentUiItem is PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection) {
                currentUiItem.copy(
                    pieChartItem = currentUiItem.pieChartItem.copy(
                        highlightedPieEntry = pieEntry
                    )
                )
            } else {
                currentUiItem
            }
        }

        _uiState.value = uiState.copy(uiItems = updateItems)
    }

    private fun makeStatsTotalSectionItem(
        currentDate: LocalDate,
        data: PurchaseNoteStatistics,
    ) = PurchaseNoteStatisticsUiItem.StatsTotalSection(
        purchaseNoteTotalCount = data.totalNoteCount,
        purchaseNoteTotalPrice = data.totalPurchasePrice,
        currentDate = currentDate
    )

    private fun makeCategoryStatsSectionItem(categoryStatsList: List<CategoryStats>) =
        PurchaseNoteStatisticsUiItem.CategoryStatsSection(
            pieChartItem = PieChartItem(
                isEmpty = categoryStatsList.isEmpty(),
                pieEntries = makeCategoryPieEntry(categoryStatsList),
                pieColors = makePieColors(categoryStatsList.take(6).size)
            ),
            childItems = makeCategoryStatsChildItems(categoryStatsList)
        )

    private fun makePaymentMethodStatsSectionItem(paymentMethodStatsList: List<PaymentMethodStats>) =
        PurchaseNoteStatisticsUiItem.PaymentMethodStatsSection(
            pieChartItem = PieChartItem(
                isEmpty = paymentMethodStatsList.isEmpty(),
                pieEntries = makePaymentMethodPieEntry(paymentMethodStatsList),
                pieColors = makePieColors(paymentMethodStatsList.take(6).size)
            ),
            childItems = makePaymentMethodStatsChildItems(paymentMethodStatsList)
        )

    private fun makeCategoryStatsChildItems(categoryStatsList: List<CategoryStats>): List<StatsContentsItem> {
        return categoryStatsList.mapIndexed { index, categoryStats ->
            StatsContentsItem.CategoryStatsItem(
                categoryStatsItem = categoryStats,
                color = AppConstants.chartColorList.getOrElse(index) {
                    AppConstants.chartColorList.last()
                }
            )
        }
    }

    private fun makePaymentMethodStatsChildItems(paymentMethodStatsList: List<PaymentMethodStats>): List<StatsContentsItem> {
        return paymentMethodStatsList.mapIndexed { index, paymentMethodStats ->
            StatsContentsItem.PaymentMethodStatsItem(
                paymentMethodStatsItem = paymentMethodStats,
                color = AppConstants.chartColorList.getOrElse(index) {
                    AppConstants.chartColorList.last()
                }
            )
        }
    }

    private fun makeCategoryPieEntry(items: List<CategoryStats>): List<PieEntry> {
        if (items.isEmpty()) return listOf(PieEntry(1f, "없음"))

        val totalPrice = items.sumOf { it.purchaseNoteTotalPrice }
        val pieEntries = items.take(MAX_PIE_ENTRIES).map { data ->
            PieEntry(
                data.purchaseNoteTotalPrice.toFloat(),
                data.categoryName,
                calculatePercentage(totalPrice, data.purchaseNoteTotalPrice)
            )
        }

        if (items.size > MAX_PIE_ENTRIES) {
            val etcTotalPrice = items.drop(MAX_PIE_ENTRIES).sumOf { it.purchaseNoteTotalPrice }
            val etcPieEntry = PieEntry(
                etcTotalPrice.toFloat(), "그 외", calculatePercentage(totalPrice, etcTotalPrice)
            )
            return pieEntries + etcPieEntry
        }

        return pieEntries
    }

    private fun makePaymentMethodPieEntry(items: List<PaymentMethodStats>): List<PieEntry> {
        if (items.isEmpty()) return listOf(PieEntry(1f, "없음"))

        val totalPrice = items.sumOf { it.purchaseNoteTotalPrice }
        val pieEntries = items.take(MAX_PIE_ENTRIES).map { data ->
            PieEntry(
                data.purchaseNoteTotalPrice.toFloat(),
                data.paymentMethodName,
                calculatePercentage(totalPrice, data.purchaseNoteTotalPrice)
            )
        }

        if (items.size > MAX_PIE_ENTRIES) {
            val etcTotalPrice = items.drop(MAX_PIE_ENTRIES).sumOf { it.purchaseNoteTotalPrice }
            val etcPieEntry = PieEntry(
                etcTotalPrice.toFloat(), "그 외", calculatePercentage(totalPrice, etcTotalPrice)
            )
            return pieEntries + etcPieEntry
        }

        return pieEntries
    }

    private fun makePieColors(entrySize: Int): List<Int> {
        if (entrySize == 0) {
            return listOf(R.color.black_400)
        }

        return (0..< entrySize).mapIndexed { index, _ ->
            AppConstants.chartColorList[index]
        }
    }

    private fun calculatePercentage(total: Long, value: Long): String {
        return if (total == 0L) "0%" else "${((value * 100) / total).toInt()}%"
    }

    companion object {
        private const val MAX_PIE_ENTRIES = 5
    }
}

data class PieChartItem(
    val isEmpty: Boolean = true,
    val pieEntries: List<PieEntry> = emptyList(),
    val pieColors: List<Int> = emptyList(),
    val highlightedPieEntry: PieEntry? = null,
)

sealed class StatsContentsItem {
    data class CategoryStatsItem(
        val categoryStatsItem: CategoryStats,
        val color: Int
    ): StatsContentsItem()

    data class PaymentMethodStatsItem(
        val paymentMethodStatsItem: PaymentMethodStats,
        val color: Int
    ): StatsContentsItem()
}

sealed class PurchaseNoteStatisticsUiItem {
    data class StatsTotalSection(
        val purchaseNoteTotalCount: Int,
        val purchaseNoteTotalPrice: Long,
        val currentDate: LocalDate
    ): PurchaseNoteStatisticsUiItem()

    data class CategoryStatsSection(
        val pieChartItem: PieChartItem,
        val childItems: List<StatsContentsItem>,
        val isExpanded: Boolean = false
    ): PurchaseNoteStatisticsUiItem()

    data class PaymentMethodStatsSection(
        val pieChartItem: PieChartItem,
        val childItems: List<StatsContentsItem>,
        val isExpanded: Boolean = false
    ): PurchaseNoteStatisticsUiItem()
}

sealed interface PurchaseNoteStatisticsUiState {
    data object Loading: PurchaseNoteStatisticsUiState
    data class Error(val error: Throwable): PurchaseNoteStatisticsUiState
    data class Success(val uiItems: List<PurchaseNoteStatisticsUiItem>):
        PurchaseNoteStatisticsUiState
}
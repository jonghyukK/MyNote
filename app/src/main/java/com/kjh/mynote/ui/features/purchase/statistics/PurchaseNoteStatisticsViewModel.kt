package com.kjh.mynote.ui.features.purchase.statistics

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
                            val paymentMethodStatsList = result.data.paymentMethodStatsList

                            val statsTotalSectionItem = makeStatsTotalSectionItem(date, result.data)
                            val categoryStatsSectionItem = makeCategoryStatsSectionItem(categoryStatsList)
                            val paymentMethodStatsSectionItem = makePaymentMethodStatsSectionItem(paymentMethodStatsList)

                            PurchaseNoteStatisticsUiState.Success(
                                listOf(statsTotalSectionItem, categoryStatsSectionItem, paymentMethodStatsSectionItem)
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

    fun updateCategoryPieHighlight(pieEntry: PieEntry?) {
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

    fun updatePaymentMethodPieHighlight(pieEntry: PieEntry?) {
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
        val items = mutableListOf<StatsContentsItem>()
        for ((index, data) in categoryStatsList.withIndex()) {
            if (index < 5) {
                items.add(
                    StatsContentsItem.CategoryStatsItem(
                        categoryStatsItem = data,
                        color = AppConstants.chartColorList[index]
                    )
                )
            } else {
                items.add(StatsContentsItem.SeeAllItem(SeeAllEvent.Category))
                break
            }
        }
        return items
    }

    private fun makePaymentMethodStatsChildItems(paymentMethodStatsList: List<PaymentMethodStats>): List<StatsContentsItem> {
        val items = mutableListOf<StatsContentsItem>()
        for ((index, data) in paymentMethodStatsList.withIndex()) {
            if (index < 5) {
                items.add(
                    StatsContentsItem.PaymentMethodStatsItem(
                        paymentMethodStatsItem = data,
                        color = AppConstants.chartColorList[index]
                    )
                )
            } else {
                items.add(StatsContentsItem.SeeAllItem(SeeAllEvent.PaymentMethod))
                break
            }
        }
        return items
    }

    private fun makeCategoryPieEntry(items: List<CategoryStats>): List<PieEntry> = when {
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

    private fun makePaymentMethodPieEntry(items: List<PaymentMethodStats>): List<PieEntry> = when {
        items.isEmpty() -> {
            listOf(PieEntry(1f, "없음"))
        }
        items.size <= 5 -> {
            items.map { data ->
                PieEntry(data.purchaseNoteTotalCount.toFloat(), data.paymentMethodName)
            }
        }
        else -> {
            val pieEntries: MutableList<PieEntry> = items.subList(0, 5).map { data ->
                PieEntry(data.purchaseNoteTotalCount.toFloat(), data.paymentMethodName)
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

sealed interface SeeAllEvent {
    data object Category: SeeAllEvent
    data object PaymentMethod: SeeAllEvent
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

    data class SeeAllItem(
        val eventType: SeeAllEvent
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
        val childItems: List<StatsContentsItem>
    ): PurchaseNoteStatisticsUiItem()

    data class PaymentMethodStatsSection(
        val pieChartItem: PieChartItem,
        val childItems: List<StatsContentsItem>
    ): PurchaseNoteStatisticsUiItem()
}

sealed interface PurchaseNoteStatisticsUiState {
    data object Loading: PurchaseNoteStatisticsUiState
    data class Error(val error: Throwable): PurchaseNoteStatisticsUiState
    data class Success(val uiItems: List<PurchaseNoteStatisticsUiItem>): PurchaseNoteStatisticsUiState
}
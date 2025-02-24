package com.kjh.mynote.ui.features.statistics.purchasenote

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.PurchaseNoteStatistics
import com.example.domain.usecase.ObservePurchaseNoteStatisticsUseCase
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.R
import com.kjh.mynote.model.CategoryStatsUiModel
import com.kjh.mynote.model.PaymentMethodStatsUiModel
import com.kjh.mynote.model.WeeklyPurchaseNoteStatsUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.isThisMonth
import com.kjh.mynote.utils.extensions.isThisYear
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */

@HiltViewModel
class PurchaseNoteStatisticsViewModel @Inject constructor(
    private val observePurchaseNoteStatisticsUseCase: ObservePurchaseNoteStatisticsUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val _queryDate = MutableStateFlow(savedStateHandle[AppConstants.INTENT_DATE] ?: LocalDate.now())
    val queryDate = _queryDate.asStateFlow()

    private val _uiState = MutableStateFlow<PurchaseNoteStatisticsUiState>(PurchaseNoteStatisticsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _queryDate.flatMapLatest { date ->
                observePurchaseNoteStatisticsUseCase(
                    startDate = date.getFirstDayOfMonth().toMillis(),
                    endDate =  date.getLastDayOfMonth().toMillis()
                ).mapResultToState(
                    onLoading = { PurchaseNoteStatisticsUiState.Loading },
                    onError =  { PurchaseNoteStatisticsUiState.Error },
                    onSuccess = { statistics ->
                        val categoryStatsList = statistics.categoryStatsList.toUiModel()
                        val paymentMethodStatsList = statistics.paymentMethodStatsList.toUiModel()
                        val weeklyStatsList = statistics.weeklyStatsList.toUiModel()

                        PurchaseNoteStatisticsUiState.Success(
                            totalStatsItem = makeStatsTotalSectionItem(date, statistics),
                            weeklyStatsItem = makeWeeklyStatsSectionItem(
                                statistics.totalPurchasePrice, statistics.totalNoteCount, weeklyStatsList),
                            categoryStatsItem = makeCategoryStatsSectionItem(categoryStatsList),
                            paymentStatsItem = makePaymentMethodStatsSectionItem(paymentMethodStatsList)
                        )
                    }
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setDate(newDate: LocalDate) {
        if (newDate == _queryDate.value) return

        _queryDate.value = newDate
    }

    fun toggleExpandForCategoryStats() {
        val uiState = uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updatedItem = uiState.categoryStatsItem.copy(isExpanded = true)

        _uiState.value = uiState.copy(categoryStatsItem = updatedItem)
    }

    fun toggleExpandForPaymentMethodStats() {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updatedItem = uiState.paymentStatsItem.copy(isExpanded = true)

        _uiState.value = uiState.copy(paymentStatsItem = updatedItem)
    }

    fun updateHighlightForCategoryPie(pieEntry: PieEntry?) {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updateItem = uiState.categoryStatsItem.copy(
            pieChartItem = uiState.categoryStatsItem.pieChartItem.copy(
                highlightedPieEntry = pieEntry
            )
        )

        _uiState.value = uiState.copy(categoryStatsItem = updateItem)
    }

    fun updateHighlightForPaymentMethodPie(pieEntry: PieEntry?) {
        val uiState = _uiState.value as? PurchaseNoteStatisticsUiState.Success ?: return
        val updateItem = uiState.paymentStatsItem.copy(
            pieChartItem = uiState.paymentStatsItem.pieChartItem.copy(
                highlightedPieEntry = pieEntry
            )
        )

        _uiState.value = uiState.copy(paymentStatsItem = updateItem)
    }

    private fun makeStatsTotalSectionItem(
        currentDate: LocalDate,
        data: PurchaseNoteStatistics,
    ) = PurchaseNoteStatisticsUiItemState.StatsTotalSection(
        purchaseNoteTotalCount = data.totalNoteCount,
        purchaseNoteTotalPrice = data.totalPurchasePrice,
        currentDate = currentDate,
        currentDateUiText = if (currentDate.isThisYear()) {
            currentDate.toStringWithPattern(AppConstants.DATE_FORMAT_M)
        } else {
            currentDate.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M)
        },
        showNextMonthBtn = !currentDate.isThisMonth()
    )

    private fun makeWeeklyStatsSectionItem(
        totalPrice: Long,
        totalCount: Int,
        weeklyStatsList: List<WeeklyPurchaseNoteStatsUiModel>
    ) = PurchaseNoteStatisticsUiItemState.WeeklyStatsSection(
        childItems = weeklyStatsList,
        totalPrice = totalPrice,
        totalCount = totalCount
    )

    private fun makeCategoryStatsSectionItem(categoryStatsList: List<CategoryStatsUiModel>) =
        PurchaseNoteStatisticsUiItemState.CategoryStatsSection(
            pieChartItem = PieChartItem(
                isEmpty = categoryStatsList.isEmpty(),
                pieEntries = makeCategoryPieEntry(categoryStatsList),
                pieColors = makePieColors(categoryStatsList.take(6).size)
            ),
            childItems = makeCategoryStatsChildItems(categoryStatsList)
        )

    private fun makePaymentMethodStatsSectionItem(paymentMethodStatsList: List<PaymentMethodStatsUiModel>) =
        PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection(
            pieChartItem = PieChartItem(
                isEmpty = paymentMethodStatsList.isEmpty(),
                pieEntries = makePaymentMethodPieEntry(paymentMethodStatsList),
                pieColors = makePieColors(paymentMethodStatsList.take(6).size)
            ),
            childItems = makePaymentMethodStatsChildItems(paymentMethodStatsList)
        )

    private fun makeCategoryStatsChildItems(categoryStatsList: List<CategoryStatsUiModel>): List<CategoryStatsItem> {
        return categoryStatsList.mapIndexed { index, categoryStats ->
            CategoryStatsItem(
                categoryStatsItem = categoryStats,
                color = AppConstants.chartColorList.getOrElse(index) {
                    AppConstants.chartColorList.last()
                }
            )
        }
    }

    private fun makePaymentMethodStatsChildItems(paymentMethodStatsList: List<PaymentMethodStatsUiModel>): List<PaymentMethodStatsItem> {
        return paymentMethodStatsList.mapIndexed { index, paymentMethodStats ->
            PaymentMethodStatsItem(
                paymentMethodStatsItem = paymentMethodStats,
                color = AppConstants.chartColorList.getOrElse(index) {
                    AppConstants.chartColorList.last()
                }
            )
        }
    }

    private fun makeCategoryPieEntry(items: List<CategoryStatsUiModel>): List<PieEntry> {
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

    private fun makePaymentMethodPieEntry(items: List<PaymentMethodStatsUiModel>): List<PieEntry> {
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

@Parcelize
data class PieChartItem(
    val isEmpty: Boolean = true,
    val pieEntries: List<PieEntry> = emptyList(),
    val pieColors: List<Int> = emptyList(),
    val highlightedPieEntry: PieEntry? = null,
): Parcelable

@Parcelize
data class CategoryStatsItem(
    val categoryStatsItem: CategoryStatsUiModel,
    val color: Int,
): Parcelable

@Parcelize
data class PaymentMethodStatsItem(
    val paymentMethodStatsItem: PaymentMethodStatsUiModel,
    val color: Int,
): Parcelable

sealed class PurchaseNoteStatisticsUiItemState {
    data class StatsTotalSection(
        val purchaseNoteTotalCount: Int,
        val purchaseNoteTotalPrice: Long,
        val currentDate: LocalDate,
        val currentDateUiText: String,
        val showNextMonthBtn: Boolean
    ): PurchaseNoteStatisticsUiItemState()

    data class WeeklyStatsSection(
        val childItems: List<WeeklyPurchaseNoteStatsUiModel>,
        val totalPrice: Long,
        val totalCount: Int
    ): PurchaseNoteStatisticsUiItemState()

    data class CategoryStatsSection(
        val pieChartItem: PieChartItem,
        val childItems: List<CategoryStatsItem>,
        val isExpanded: Boolean = false
    ): PurchaseNoteStatisticsUiItemState() {
        val isVisibleMoreBtn = !isExpanded && childItems.size > 5
    }

    data class PaymentMethodStatsSection(
        val pieChartItem: PieChartItem,
        val childItems: List<PaymentMethodStatsItem>,
        val isExpanded: Boolean = false
    ): PurchaseNoteStatisticsUiItemState() {
        val isVisibleMoreBtn = !isExpanded && childItems.size > 5
    }
}

sealed interface PurchaseNoteStatisticsUiState {
    data object Loading: PurchaseNoteStatisticsUiState
    data object Error: PurchaseNoteStatisticsUiState
    data class Success(
        val totalStatsItem: PurchaseNoteStatisticsUiItemState.StatsTotalSection,
        val weeklyStatsItem: PurchaseNoteStatisticsUiItemState.WeeklyStatsSection,
        val categoryStatsItem: PurchaseNoteStatisticsUiItemState.CategoryStatsSection,
        val paymentStatsItem: PurchaseNoteStatisticsUiItemState.PaymentMethodStatsSection
    ): PurchaseNoteStatisticsUiState
}
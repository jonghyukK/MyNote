package com.kjh.mynote.ui.features.category.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.GetCategoryStatisticsUseCase
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 14..
 * Description:
 */

@HiltViewModel
class CategoryStatisticsViewModel @Inject constructor(
    private val getCategoryStatisticsUseCase: GetCategoryStatisticsUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _currentCategory = MutableStateFlow(
        savedStateHandle.get<CategoryUiModel>(AppConstants.INTENT_CATEGORY_ITEM)!!)
    val currentCategory = _currentCategory.asStateFlow()

    private val _currentDate = MutableStateFlow(
        savedStateHandle.get<LocalDate>(AppConstants.INTENT_DATE) ?: LocalDate.now())
    val currentDate = _currentDate.asStateFlow()

    private val categoryStatsFlow = getCategoryStatsFlow(
        _currentCategory, _currentDate, getCategoryStatisticsUseCase
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApiResult.Loading
        )

    private val purchaseNotesFlow = getPurchaseNotesFlow(
        _currentCategory, _currentDate, getFilteredSearchPurchaseNotesUseCase
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApiResult.Loading
        )

    val uiState = combine(
        categoryStatsFlow, purchaseNotesFlow
    ) { categoryStatsResult, purchaseNotesResult ->
        when {
            purchaseNotesResult is ApiResult.Loading
                    || categoryStatsResult is ApiResult.Loading -> {
                CategoryStatisticsUiState.Loading
            }

            purchaseNotesResult is ApiResult.Error -> {
                CategoryStatisticsUiState.Error(purchaseNotesResult.error)
            }

            categoryStatsResult is ApiResult.Error -> {
                CategoryStatisticsUiState.Error(categoryStatsResult.error)
            }

            purchaseNotesResult is ApiResult.Success
                    && categoryStatsResult is ApiResult.Success -> {

                val purchaseNoteUiItems = makePurchaseNoteUiItems(purchaseNotesResult.data)
                val statsInfoUiItem = makeStatsInfoUiItem(categoryStatsResult.data, _currentCategory.value)

                val uiItems = if (categoryStatsResult.data.purchaseNameStats.isEmpty()) {
                    listOf(statsInfoUiItem)
                } else {
                    listOf(statsInfoUiItem) + makePurchaseNameStatsUiItem(categoryStatsResult.data)
                }

                CategoryStatisticsUiState.CategoryStatistics(uiItems + purchaseNoteUiItems)
            }

            else -> CategoryStatisticsUiState.Error(IllegalStateException("Unexpected State"))
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryStatisticsUiState.Loading
        )

    fun setCategory(newCategory: CategoryUiModel) {
        if (_currentCategory.value == newCategory) return

        _currentCategory.value = newCategory
    }

    fun setDate(newDate: LocalDate) {
        if (_currentDate.value == newDate) return

        _currentDate.value = newDate
    }

    private fun makeStatsInfoUiItem(data: CategoryPurchaseNoteStats, currentCategory: CategoryUiModel) =
        CategoryPurchaseNoteStatsUiItems.StatsInfoItem(
            currentCategory = currentCategory,
            totalNoteCount = data.categoryTotalCount,
            totalNotePrice = data.categoryTotalPrice
        )

    private fun makePurchaseNameStatsUiItem(data: CategoryPurchaseNoteStats) =
        CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem(
            purchaseNameStatsItems = data.purchaseNameStats.take(5).mapIndexed { index, item ->
                PurchaseNameStatsItem(
                    purchaseName = item.purchaseName,
                    totalCount = item.totalCount,
                    totalPrice = item.totalPrice,
                    maxCount = data.purchaseNameStats.maxOfOrNull { it.totalCount } ?: 0,
                    color = AppConstants.chartColorAlphaList[index]
                )
            }
        )

    private fun makePurchaseNoteUiItems(purchaseNotes: List<PurchaseNote>) =
        if (purchaseNotes.isEmpty()) {
            listOf(CategoryPurchaseNoteStatsUiItems.Empty)
        } else {
            purchaseNotes.toUiModel().groupBy { it.localDate }
                .flatMap { (date, items) ->
                    listOf(CategoryPurchaseNoteStatsUiItems.PurchaseNoteDateItem(date)) +
                            items.map {
                                CategoryPurchaseNoteStatsUiItems.PurchaseNoteItem(it)
                            }
                }
        }
}

private fun getPurchaseNotesFlow(
    currentCategory: StateFlow<CategoryUiModel>,
    currentDate: StateFlow<LocalDate>,
    getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
) = combine(currentCategory, currentDate) { category, date ->
    category to date
}.flatMapLatest { (category, date) ->
    getFilteredSearchPurchaseNotesUseCase(
        categoryIds = listOf(category.id),
        startDate = date.getFirstDayOfMonth().toMillis(),
        endDate = date.getLastDayOfMonth().toMillis()
    )
}

private fun getCategoryStatsFlow(
    currentCategory: StateFlow<CategoryUiModel>,
    currentDate: StateFlow<LocalDate>,
    getCategoryStatisticsUseCase: GetCategoryStatisticsUseCase,
) = combine(currentCategory, currentDate) { category, date ->
    category to date
}.flatMapLatest { (category, date) ->
    getCategoryStatisticsUseCase(
        categoryId = category.id,
        startDate = date.getFirstDayOfMonth().toMillis(),
        endDate = date.getLastDayOfMonth().toMillis()
    )
}

data class PurchaseNameStatsItem(
    val purchaseName: String,
    val totalCount: Int,
    val totalPrice: Long,
    val maxCount: Int,
    val color: Int
)

sealed class CategoryPurchaseNoteStatsUiItems {
    data object Empty: CategoryPurchaseNoteStatsUiItems()

    data class StatsInfoItem(
        val currentCategory: CategoryUiModel? = null,
        val totalNoteCount: Int = 0,
        val totalNotePrice: Long = 0
    ): CategoryPurchaseNoteStatsUiItems()

    data class PurchaseNameRankingItem(
        val purchaseNameStatsItems: List<PurchaseNameStatsItem>
    ): CategoryPurchaseNoteStatsUiItems()

    data class PurchaseNoteDateItem(
        val date: LocalDate
    ): CategoryPurchaseNoteStatsUiItems()

    data class PurchaseNoteItem(
        val purchaseNote: PurchaseNoteUiModel
    ): CategoryPurchaseNoteStatsUiItems()
}

sealed interface CategoryStatisticsUiState {
    data object Loading: CategoryStatisticsUiState
    data class Error(val error: Throwable): CategoryStatisticsUiState
    data class CategoryStatistics(val items: List<CategoryPurchaseNoteStatsUiItems>): CategoryStatisticsUiState
}

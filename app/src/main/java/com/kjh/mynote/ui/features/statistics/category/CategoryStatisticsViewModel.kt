package com.kjh.mynote.ui.features.statistics.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CategoryStatsDetail
import com.example.domain.model.PurchaseNameStats
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.ObserveCategoryStatisticsUseCase
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNameStatsUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
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
    private val observeCategoryStatisticsUseCase: ObserveCategoryStatisticsUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val _currentCategory = MutableStateFlow<CategoryUiModel>(
        savedStateHandle[AppConstants.INTENT_CATEGORY_ITEM]
            ?: throw IllegalStateException("CategoryStatisticsViewModel is required in SavedStateHandle")
    )
    val currentCategory = _currentCategory.asStateFlow()

    private val _currentDate =
        MutableStateFlow(savedStateHandle[AppConstants.INTENT_DATE] ?: LocalDate.now())
    val currentDate = _currentDate.asStateFlow()

    val uiState: StateFlow<CategoryStatisticsUiState> =
        combine(_currentCategory, _currentDate) { currentCategory, currentDate ->
            currentCategory to currentDate
        }
            .flatMapLatest { (category, date) ->
                combineApiResults(
                    observeCategoryStatisticsUseCase(
                        categoryId = category.id,
                        startDate = date.getFirstDayOfMonth().toMillis(),
                        endDate = date.getLastDayOfMonth().toMillis()
                    ),
                    getFilteredSearchPurchaseNotesUseCase(
                        categoryIds = listOf(category.id),
                        startDate = date.getFirstDayOfMonth().toMillis(),
                        endDate = date.getLastDayOfMonth().toMillis()
                    ),
                    onLoading = { CategoryStatisticsUiState.Loading },
                    onError = { CategoryStatisticsUiState.Error }
                ) { categoryStatsDetail, purchaseNotes ->
                    val uiItems = makeUiItems(category, categoryStatsDetail, purchaseNotes)
                    CategoryStatisticsUiState.Success(uiItems = uiItems)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                CategoryStatisticsUiState.Loading
            )

    fun setCategory(newCategory: CategoryUiModel) {
        if (_currentCategory.value == newCategory) return

        _currentCategory.value = newCategory
    }

    fun setDate(newDate: LocalDate) {
        if (_currentDate.value == newDate) return

        _currentDate.value = newDate
    }

    private fun makeUiItems(
        category: CategoryUiModel,
        categoryStatsDetail: CategoryStatsDetail,
        purchaseNotes: List<PurchaseNote>,
    ): List<CategoryStatisticsUiItem> {
        val uiItems: MutableList<CategoryStatisticsUiItem> = mutableListOf()

        val statsInfoItem = makeStatsInfoUiItem(category, categoryStatsDetail)
        uiItems.add(statsInfoItem)

        if (categoryStatsDetail.categoryStats == null) {
            uiItems.add(CategoryStatisticsUiItem.Empty)
        } else {
            val purchaseNameStats =
                makePurchaseNameStatsUiItem(categoryStatsDetail.purchaseNameStatsList)
            uiItems.add(purchaseNameStats)

            val purchaseNoteItems = makePurchaseNoteUiItems(purchaseNotes.toUiModel())
            uiItems.addAll(purchaseNoteItems)
        }

        return uiItems
    }

    private fun makeStatsInfoUiItem(
        category: CategoryUiModel,
        categoryStatsDetail: CategoryStatsDetail
    ) = CategoryStatisticsUiItem.StatsInfo(
        currentCategory = category,
        totalNoteCount = categoryStatsDetail.categoryStats?.purchaseNoteTotalCount ?: 0,
        totalNotePrice = categoryStatsDetail.categoryStats?.purchaseNoteTotalPrice ?: 0
    )

    private fun makePurchaseNameStatsUiItem(purchaseNameStatsList: List<PurchaseNameStats>) =
        CategoryStatisticsUiItem.PurchaseNameStats(
            purchaseNameStatsList.mapIndexed { index, item ->
                PurchaseNameStatsItem(
                    purchaseNameStats = item.toUiModel(),
                    maxCount = purchaseNameStatsList.maxOfOrNull { it.totalCount } ?: 0,
                    color = AppConstants.chartColorAlphaList.getOrElse(index) {
                        AppConstants.chartColorAlphaList.last()
                    }
                )
            }
        )

    private fun makePurchaseNoteUiItems(purchaseNotes: List<PurchaseNoteUiModel>) =
        purchaseNotes.groupBy { it.localDate }
            .flatMap { (date, items) ->
                listOf(CategoryStatisticsUiItem.PurchaseNoteDate(date)) + items.map {
                    CategoryStatisticsUiItem.PurchaseNoteContents(it)
                }
            }
}

data class PurchaseNameStatsItem(
    val purchaseNameStats: PurchaseNameStatsUiModel,
    val maxCount: Int,
    val color: Int
)

sealed interface CategoryStatisticsUiItem {
    data class StatsInfo (
        val currentCategory: CategoryUiModel,
        val totalNoteCount: Int,
        val totalNotePrice: Long,
    ): CategoryStatisticsUiItem

    data class PurchaseNameStats(
        val purchaseNameStatsItems: List<PurchaseNameStatsItem>
    ): CategoryStatisticsUiItem

    data class PurchaseNoteDate(
        val date: LocalDate
    ): CategoryStatisticsUiItem

    data class PurchaseNoteContents(
        val purchaseNote: PurchaseNoteUiModel
    ): CategoryStatisticsUiItem

    data object Empty: CategoryStatisticsUiItem
}

sealed interface CategoryStatisticsUiState {
    data object Loading: CategoryStatisticsUiState
    data object Error: CategoryStatisticsUiState
    data class Success(val uiItems: List<CategoryStatisticsUiItem>): CategoryStatisticsUiState
}

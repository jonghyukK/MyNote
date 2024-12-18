package com.kjh.mynote.ui.features.category.purchasestats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.onError
import com.example.domain.model.onLoading
import com.example.domain.model.onSuccess
import com.example.domain.usecase.GetCategoryWithPurchaseNoteStatsUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 14..
 * Description:
 */

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

data class CategoryPurchaseNoteStatsUiState(
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val currentCategory: CategoryUiModel? = null,
    val currentDate: LocalDate = LocalDate.now(),
    val uiItems: List<CategoryPurchaseNoteStatsUiItems> = emptyList()
)

@HiltViewModel
class CategoryPurchaseNoteStatsViewModel @Inject constructor(
    private val getCategoryWithPurchaseNoteStatsUseCase: GetCategoryWithPurchaseNoteStatsUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(CategoryPurchaseNoteStatsUiState(
        currentCategory = savedStateHandle.get<CategoryUiModel>(AppConstants.INTENT_CATEGORY_ITEM)
    ))
    val uiState = _uiState.asStateFlow()

    init {
        getCategoryPurchaseNoteStats()
    }

    fun getCategoryPurchaseNoteStats() {
        viewModelScope.launch {
            val categoryId = _uiState.value.currentCategory?.id ?: return@launch
            val dateRange = _uiState.value.currentDate

            val startDate = dateRange.getFirstDayOfMonth().toMillis()
            val endDate = dateRange.getLastDayOfMonth().toMillis()

            getCategoryWithPurchaseNoteStatsUseCase(
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate
            ).collect { result ->
                result
                    .onLoading {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }
                    .onError { error ->
                        _uiState.value = CategoryPurchaseNoteStatsUiState(
                            isLoading = false,
                            errorMsg = error.message ?: "카테고리별 구매노트 정보를 불러오는데 실패하였습니다."
                        )
                    }
                    .onSuccess { data ->
                        val statsInfoItem = makeStatsInfoItem(data)
                        val purchaseNoteItems = makePurchaseNoteItems(data)

                        val uiItems = if (data.purchaseNameStatsList.isEmpty()) {
                            listOf(statsInfoItem) + purchaseNoteItems
                        } else {
                            val purchaseNameRankingItem = makePurchaseNameRankingItem(data)

                            listOf(statsInfoItem) + purchaseNameRankingItem + purchaseNoteItems
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

    fun setCategory(newCategory: CategoryUiModel) {
        if (_uiState.value.currentCategory?.id == newCategory.id) return

        _uiState.update {
            it.copy(
                currentCategory = newCategory
            )
        }

        getCategoryPurchaseNoteStats()
    }

    fun setDate(newDate: LocalDate) {
        if (_uiState.value.currentDate == newDate) return

        _uiState.update {
            it.copy(
                currentDate = newDate
            )
        }

        getCategoryPurchaseNoteStats()
    }

    fun shownError() {
        _uiState.update {
            it.copy(errorMsg = null)
        }
    }

    private fun makePurchaseNoteItems(data: CategoryPurchaseNoteStats): List<CategoryPurchaseNoteStatsUiItems> {
        if (data.purchaseNoteList.isEmpty()) {
            return listOf(CategoryPurchaseNoteStatsUiItems.Empty)
        }

        return data.purchaseNoteList.toUiModel().flatMap { model ->
            listOf(CategoryPurchaseNoteStatsUiItems.PurchaseNoteDateItem(model.date!!)) +
                    model.purchaseNoteItems.map {
                        CategoryPurchaseNoteStatsUiItems.PurchaseNoteItem(it)
                    }
        }
    }

    private fun makeStatsInfoItem(data: CategoryPurchaseNoteStats) =
        CategoryPurchaseNoteStatsUiItems.StatsInfoItem(
            currentCategory = _uiState.value.currentCategory,
            totalNoteCount = data.categoryTotalCount,
            totalNotePrice = data.categoryTotalPrice
        )

    private fun makePurchaseNameRankingItem(data: CategoryPurchaseNoteStats)
    : CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem {
        val maxCount = data.purchaseNameStatsList.maxOfOrNull { it.totalCount } ?: 0

        return CategoryPurchaseNoteStatsUiItems.PurchaseNameRankingItem(
            purchaseNameStatsItems = data.purchaseNameStatsList.take(5).mapIndexed { index, item ->
                PurchaseNameStatsItem(
                    purchaseName = item.purchaseName,
                    totalCount = item.totalCount,
                    totalPrice = item.totalPrice,
                    maxCount = maxCount,
                    color = AppConstants.chartColorAlphaList[index]
                )
            }
        )
    }
}
package com.kjh.mynote.ui.features.category.purchasestats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.model.SortType
import com.example.domain.model.onError
import com.example.domain.model.onLoading
import com.example.domain.model.onSuccess
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.common.uistate.PurchaseNotesUiState
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
 * Created On 2024. 12. 14..
 * Description:
 */

data class CategoryPurchaseNoteStatsUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val errorMsg: String? = null,
    val totalNoteCount: Int = 0,
    val totalNotePrice: Long = 0,
    val purchaseNotes: List<PurchaseNotesUiState> = emptyList()
)

@HiltViewModel
class CategoryPurchaseNoteStatsViewModel @Inject constructor(
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _currentCategory = MutableStateFlow(savedStateHandle.get<CategoryUiModel>(AppConstants.INTENT_CATEGORY_ITEM))
    val currentCategory = _currentCategory.asStateFlow()

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate = _currentDate.asStateFlow()

    private val _uiState = MutableStateFlow(CategoryPurchaseNoteStatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getPurchaseNotes()
    }

    fun getPurchaseNotes() {
        viewModelScope.launch {
            val categoryId = _currentCategory.value?.id ?: return@launch
            val dateRange = _currentDate.value

            val startDate = dateRange.getFirstDayOfMonth()
            val endDate = dateRange.getLastDayOfMonth()

            getFilteredSearchPurchaseNotesUseCase(
                queryText = "",
                startDate = startDate.toMillis(),
                endDate = endDate.toMillis(),
                minPrice = AppConstants.PRICE_MIN_LIMIT,
                maxPrice = AppConstants.PRICE_MAX_LIMIT,
                categoryIds = listOf(categoryId),
                sortType = SortType.LATEST
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
                            errorMsg = error.message ?: "구매노트 목록 조회가 실패하였습니다."
                        )
                    }
                    .onSuccess { data ->
                        val resultItems = data.toUiModel()

                        when {
                            resultItems.isEmpty() -> {
                                _uiState.value = CategoryPurchaseNoteStatsUiState(
                                    isLoading = false,
                                    isEmpty = true
                                )
                            }
                            else -> {
                                val totalCount = resultItems.sumOf { it.purchaseNoteItems.size }
                                val totalPrice = resultItems.sumOf { it.purchaseNoteItems.sumOf { it.purchasePrice } }

                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isEmpty = false,
                                        totalNoteCount = totalCount,
                                        totalNotePrice = totalPrice,
                                        purchaseNotes = resultItems.flatMap { model ->
                                            listOf(PurchaseNotesUiState.DateItem(model.date!!)) +
                                                    model.purchaseNoteItems.map {
                                                        PurchaseNotesUiState.PurchaseNoteItem(it)
                                                    }
                                        }
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }

    fun setCategory(category: CategoryUiModel) {
        _currentCategory.value = category
        getPurchaseNotes()
    }

    fun setDate(date: LocalDate) {
        _currentDate.value = date
        getPurchaseNotes()
    }
}
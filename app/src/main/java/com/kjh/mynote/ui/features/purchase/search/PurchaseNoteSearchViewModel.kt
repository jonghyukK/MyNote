package com.kjh.mynote.ui.features.purchase.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.example.domain.usecase.GetMaxPurchasePriceUseCase
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.features.category.list.CategoryListItem
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

sealed class PurchaseNoteSearchUiState {
    data class FilterItem(
        val purchaseNoteSearchFilterUiState: PurchaseNoteSearchFilterUiState
    ): PurchaseNoteSearchUiState()

    data object Loading: PurchaseNoteSearchUiState()

    data object Empty: PurchaseNoteSearchUiState()

    data class DateItem(val date: LocalDate): PurchaseNoteSearchUiState()

    data class ResultItem(val purchaseNoteItem: PurchaseNoteUiModel): PurchaseNoteSearchUiState()
}

data class PriceFilter(
    val minPrice: Long = AppConstants.PRICE_MIN_LIMIT,
    val maxPrice: Long = AppConstants.PRICE_MAX_LIMIT,
    val myMaxPrice: Long = AppConstants.PRICE_MAX_LIMIT
)

fun PriceFilter.isChanged() =
    minPrice != AppConstants.PRICE_MIN_LIMIT ||
            maxPrice != myMaxPrice

data class PurchaseNoteSearchFilterUiState(
    val purchaseName: String = "",
    val categoryItems: List<CategoryListItem> = emptyList(),
    val dateRangeFilter: DateRangeFilter? = null,
    val priceFilter: PriceFilter = PriceFilter()
)

@HiltViewModel
class PurchaseNoteSearchViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getMaxPurchasePriceUseCase: GetMaxPurchasePriceUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<List<PurchaseNoteSearchUiState>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _filtersUiState = MutableStateFlow(PurchaseNoteSearchFilterUiState())
    val filtersUiState = _filtersUiState.asStateFlow()

    init {
        makeCategoryFilterItem()

        viewModelScope.launch {
            _filtersUiState.flatMapLatest { filterUiState ->
                val (startDate, endDate) = getStartDateAndEndDateTimeMillis(filterUiState.dateRangeFilter)
                val categoryIds = filterUiState.categoryItems.filter { it.isSelected }
                    .map { it.categoryItem.id }

                getFilteredSearchPurchaseNotesUseCase(
                    queryText = filterUiState.purchaseName,
                    startDate = startDate,
                    endDate = endDate,
                    minPrice = filterUiState.priceFilter.minPrice,
                    maxPrice = filterUiState.priceFilter.maxPrice,
                    categoryIds = categoryIds
                ).map { result ->
                    when (result) {
                        is Result.Loading -> listOf(PurchaseNoteSearchUiState.Loading)
                        is Result.Error -> listOf(PurchaseNoteSearchUiState.Empty)
                        is Result.Success -> {
                            delay(400)
                            val resultItems = result.data?.toUiModel() ?: emptyList()
                            if (resultItems.isEmpty()) {
                                listOf(PurchaseNoteSearchUiState.Empty)
                            } else {
                                resultItems.flatMap { model ->
                                    listOf(PurchaseNoteSearchUiState.DateItem(model.date)) +
                                            model.purchaseNoteItems.map {
                                                PurchaseNoteSearchUiState.ResultItem(it)
                                            }
                                }
                            }
                        }
                    }
                }
            }.collectLatest { data ->
                val filterItem = PurchaseNoteSearchUiState.FilterItem(_filtersUiState.value)
                _uiState.value = listOf(filterItem) + data
            }
        }
    }

    fun addOrDeleteCategoryItemBy(categoryId: Int) {
        val updatedCategoryItem = _filtersUiState.value.categoryItems.map { currentCategoryItem ->
            if (currentCategoryItem.categoryItem.id == categoryId) {
                currentCategoryItem.copy(
                    isSelected = !currentCategoryItem.isSelected
                )
            } else {
                currentCategoryItem
            }
        }

        _filtersUiState.update {
            it.copy(categoryItems = updatedCategoryItem)
        }
    }

    fun setPurchaseName(name: String) {
        if (name == _filtersUiState.value.purchaseName) return

        _filtersUiState.update {
            it.copy(purchaseName = name)
        }
    }

    fun setDateRangeFilter(dateRangeFilter: DateRangeFilter?) {
        _filtersUiState.update {
            it.copy(dateRangeFilter = dateRangeFilter)
        }
    }

    fun setPriceFilter(priceFilter: PriceFilter) {
        _filtersUiState.update {
            it.copy(priceFilter = priceFilter)
        }
    }

    fun shownErrorMsg() {
//        _searchUiState.value = PurchaseNoteSearchResultUiState.Init
    }

    private fun makeCategoryFilterItem() {
        viewModelScope.launch {
            combine(
                getAllCategoriesUseCase(),
                getMaxPurchasePriceUseCase()
            ) { categories, myMaxPrice ->
                val categoryItems = categories.map {
                    CategoryListItem(categoryItem = it.toUiModel())
                }

                val maxPriceValue = myMaxPrice ?: AppConstants.PRICE_MAX_LIMIT

                PurchaseNoteSearchFilterUiState(
                    categoryItems = categoryItems,
                    priceFilter = PriceFilter(
                        maxPrice = maxPriceValue,
                        myMaxPrice = maxPriceValue
                    )
                )
            }.collectLatest {
                _filtersUiState.value = it
            }
        }
    }


    private fun getStartDateAndEndDateTimeMillis(monthFilter: DateRangeFilter?): Pair<Long, Long> =
        when (monthFilter) {
            is DateRangeFilter.Monthly -> {
                Pair(
                    monthFilter.date.getFirstDayOfMonth().toMillis(),
                    monthFilter.date.getLastDayOfMonth().toMillis()
                )
            }
            is DateRangeFilter.MonthOne -> {
                Pair(
                    LocalDate.now().minusMonths(1).toMillis(),
                    LocalDate.now().toMillis()
                )
            }
            is DateRangeFilter.MonthThree -> {
                Pair(
                    LocalDate.now().minusMonths(3).toMillis(),
                    LocalDate.now().toMillis()
                )
            }
            is DateRangeFilter.Directly -> {
                Pair(
                    monthFilter.startDate.toMillis(),
                    monthFilter.endDate.toMillis()
                )
            }
            else -> {
                Pair(
                    LocalDate.now().minusYears(1).toMillis(),
                    LocalDate.now().toMillis()
                )
            }
        }
}
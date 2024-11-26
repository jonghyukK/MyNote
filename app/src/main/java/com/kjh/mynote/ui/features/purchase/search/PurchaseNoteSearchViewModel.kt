package com.kjh.mynote.ui.features.purchase.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.example.domain.usecase.GetMaxPurchasePriceUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
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

    data class AppliedFilterItems(
        val filterItems: List<Filters> = emptyList()
    ): PurchaseNoteSearchUiState()

    data class DateItem(val date: LocalDate): PurchaseNoteSearchUiState()

    data class ResultItem(val purchaseNoteItem: PurchaseNoteUiModel): PurchaseNoteSearchUiState()
}

data class PurchaseNoteSearchFilterUiState(
    val purchaseNameFilter: Filters.PurchaseName = Filters.PurchaseName(),
    val categoryFilters: List<Filters.Category> = emptyList(),
    val dateRangeFilter: Filters.DateRange = Filters.DateRange(),
    val priceFilter: Filters.Price = Filters.Price()
)

sealed class Filters {
    data class PurchaseName(
        val purchaseName: String = "",
        val isApplied: Boolean = false
    ): Filters()

    data class Category(
        val categoryItem: CategoryUiModel,
        val isApplied: Boolean = false
    ): Filters()

    data class DateRange(
        val dateRangeFilter: DateRangeFilter? = null,
        val isApplied: Boolean = false
    ): Filters()

    data class Price(
        val minPrice: Long = AppConstants.PRICE_MIN_LIMIT,
        val maxPrice: Long = AppConstants.PRICE_MAX_LIMIT,
        val myMaxPrice: Long = AppConstants.PRICE_MAX_LIMIT,
        val isApplied: Boolean = false
    ): Filters()
}

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

    private val _appliedFilterItem = MutableStateFlow<List<Filters>>(emptyList())
    val appliedFilterItem = _appliedFilterItem.asStateFlow()

    init {
        makeCategoryFilterItem()

        viewModelScope.launch {
            _filtersUiState.flatMapLatest { filterUiState ->
                val (startDate, endDate) = getStartDateAndEndDateTimeMillis(filterUiState.dateRangeFilter.dateRangeFilter)
                val categoryIds = filterUiState.categoryFilters.filter { it.isApplied }
                    .map { it.categoryItem.id }

                getFilteredSearchPurchaseNotesUseCase(
                    queryText = filterUiState.purchaseNameFilter.purchaseName,
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
                            delay(300)
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
                val appliedFilterItem = PurchaseNoteSearchUiState.AppliedFilterItems(_appliedFilterItem.value)

                if (appliedFilterItem.filterItems.isEmpty()) {
                    _uiState.value = listOf(filterItem) + data
                } else {
                    _uiState.value = listOf(filterItem, appliedFilterItem) + data
                }
            }
        }
    }

    fun addOrDeleteCategoryItemBy(categoryId: Int) {
        val updatedCategoryItem = _filtersUiState.value.categoryFilters.map { currentCategoryItem ->
            if (currentCategoryItem.categoryItem.id == categoryId) {
                updateCategoryFilter(currentCategoryItem)

                currentCategoryItem.copy(
                    isApplied = !currentCategoryItem.isApplied
                )
            } else {
                currentCategoryItem
            }
        }

        _filtersUiState.update {
            it.copy(categoryFilters = updatedCategoryItem)
        }
    }

    fun setPurchaseName(filter: Filters.PurchaseName) {
        _filtersUiState.update {
            it.copy(purchaseNameFilter = filter)
        }

        updatePurchaseNameFilter(filter)
    }

    fun setDateRangeFilter(dateRangeFilter: Filters.DateRange) {
        _filtersUiState.update {
            it.copy(dateRangeFilter = dateRangeFilter)
        }

        updateDateFilter(dateRangeFilter)
    }

    fun setPriceFilter(priceFilter: Filters.Price) {
        _filtersUiState.update {
            it.copy(priceFilter = priceFilter)
        }

        updatePriceFilter(priceFilter)
    }

    fun deleteFilter(filters: Filters) {
        when (filters) {
            is Filters.Category -> {
                addOrDeleteCategoryItemBy(filters.categoryItem.id)
            }
            is Filters.DateRange -> {
                setDateRangeFilter(Filters.DateRange())
            }
            is Filters.Price -> {
                setPriceFilter(Filters.Price())
            }
            is Filters.PurchaseName -> {
                setPurchaseName(Filters.PurchaseName())
            }
        }
    }

    private fun updateCategoryFilter(categoryFilter: Filters.Category) {
        val filters = _appliedFilterItem.value.toMutableList()

        val existingFilterIndex = filters.indexOfFirst {
            (it is Filters.Category) && (it.categoryItem.categoryName == categoryFilter.categoryItem.categoryName)
        }
        if (existingFilterIndex != -1) {
            filters.removeAt(existingFilterIndex)
        } else {
            filters.add(categoryFilter)
        }

        _appliedFilterItem.value = filters
    }

    private fun updatePurchaseNameFilter(newPurchaseName: Filters.PurchaseName) {
        val filters = _appliedFilterItem.value.toMutableList()

        val existingFilterIndex = filters.indexOfFirst { it is Filters.PurchaseName }
        if (existingFilterIndex != -1) {
            if (newPurchaseName.isApplied) {
                filters[existingFilterIndex] = newPurchaseName
            } else {
                filters.removeAt(existingFilterIndex)
            }
        } else {
            filters.add(newPurchaseName)
        }

        _appliedFilterItem.value = filters
    }

    private fun updateDateFilter(dateFilter: Filters.DateRange) {
        val filters = _appliedFilterItem.value.toMutableList()

        val existingFilterIndex = filters.indexOfFirst { it is Filters.DateRange }
        if (existingFilterIndex != -1) {
            if (dateFilter.isApplied) {
                filters[existingFilterIndex] = dateFilter
            } else {
                filters.removeAt(existingFilterIndex)
            }
        } else {
            filters.add(dateFilter)
        }

        _appliedFilterItem.value = filters
    }

    private fun updatePriceFilter(priceFilter: Filters.Price) {
        val filters = _appliedFilterItem.value.toMutableList()

        val existingFilterIndex = filters.indexOfFirst { it is Filters.Price }
        if (existingFilterIndex != -1) {
            if (priceFilter.isApplied) {
                filters[existingFilterIndex] = priceFilter
            } else {
                filters.removeAt(existingFilterIndex)
            }
        } else {
            filters.add(priceFilter)
        }

        _appliedFilterItem.value = filters
    }

    private fun makeCategoryFilterItem() {
        viewModelScope.launch {
            combine(
                getAllCategoriesUseCase(),
                getMaxPurchasePriceUseCase()
            ) { categories, myMaxPrice ->
                val categoryItems = categories.map {
                    Filters.Category(categoryItem = it.toUiModel())
                }

                val maxPriceValue = myMaxPrice ?: AppConstants.PRICE_MAX_LIMIT

                PurchaseNoteSearchFilterUiState(
                    categoryFilters = categoryItems,
                    priceFilter = Filters.Price(
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
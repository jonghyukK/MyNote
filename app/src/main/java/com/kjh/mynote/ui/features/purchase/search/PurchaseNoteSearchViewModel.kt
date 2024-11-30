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
import kotlin.reflect.KClass

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

sealed class PurchaseNoteSearchUiState {
    data object Loading: PurchaseNoteSearchUiState()
    data object Empty: PurchaseNoteSearchUiState()
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
    abstract fun isApplied(): Boolean

    data class PurchaseName(
        val purchaseName: String = ""
    ): Filters() {
        override fun isApplied(): Boolean = purchaseName.isNotBlank()
    }

    data class Category(
        val categoryItem: CategoryUiModel,
        val isSelected: Boolean = false
    ): Filters() {
        override fun isApplied(): Boolean = isSelected
    }

    data class DateRange(
        val dateRangeFilter: DateRangeFilter = DateRangeFilter.Monthly()
    ): Filters() {
        override fun isApplied(): Boolean = true
    }

    data class Price(
        val minPrice: Long? = null,
        val maxPrice: Long? = null,
        val myMaxPrice: Long = AppConstants.PRICE_MAX_LIMIT
    ): Filters() {
        override fun isApplied(): Boolean =
            minPrice != null || maxPrice != null
    }
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

    private val _resultTotalCount = MutableStateFlow(0)
    val resultTotalCount = _resultTotalCount.asStateFlow()

    init {
        makeCategoryFilterItem()

        viewModelScope.launch {
            _filtersUiState.flatMapLatest { filterUiState ->
                val (startDate, endDate) = getStartDateAndEndDateTimeMillis(filterUiState.dateRangeFilter.dateRangeFilter)
                val categoryIds = filterUiState.categoryFilters.filter { it.isApplied() }
                    .map { it.categoryItem.id }

                getFilteredSearchPurchaseNotesUseCase(
                    queryText = filterUiState.purchaseNameFilter.purchaseName,
                    startDate = startDate,
                    endDate = endDate,
                    minPrice = filterUiState.priceFilter.minPrice ?: AppConstants.PRICE_MIN_LIMIT,
                    maxPrice = filterUiState.priceFilter.maxPrice ?: filterUiState.priceFilter.myMaxPrice,
                    categoryIds = categoryIds
                ).map { result ->
                    when (result) {
                        is Result.Loading -> listOf(PurchaseNoteSearchUiState.Loading)
                        is Result.Error -> listOf(PurchaseNoteSearchUiState.Empty)
                        is Result.Success -> {
                            delay(300)

                            val resultItems = result.data?.toUiModel() ?: emptyList()

                            _resultTotalCount.value = resultItems.sumOf { it.purchaseNoteItems.size }

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
                _uiState.value = data
            }
        }
    }

    fun setDateRangeFilter(dateRangeFilter: Filters.DateRange) {
        _filtersUiState.update {
            it.copy(dateRangeFilter = dateRangeFilter)
        }
    }

    fun addOrDeleteCategoryItemBy(categoryId: Int) {
        val updatedCategoryItem = _filtersUiState.value.categoryFilters.map { currentCategoryItem ->
            if (currentCategoryItem.categoryItem.id == categoryId) {
                updateAppliedFilters(currentCategoryItem, Filters.Category::class)

                currentCategoryItem.copy(
                    isSelected = !currentCategoryItem.isSelected
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

        updateAppliedFilters(filter, Filters.PurchaseName::class)
    }

    fun setPriceFilter(priceFilter: Filters.Price) {
        _filtersUiState.update {
            it.copy(priceFilter = priceFilter)
        }

        updateAppliedFilters(priceFilter, Filters.Price::class)
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
                setPriceFilter(Filters.Price(myMaxPrice = filters.myMaxPrice))
            }
            is Filters.PurchaseName -> {
                setPurchaseName(Filters.PurchaseName())
            }
        }
    }

    fun applyAllFilters(filterUiState: PurchaseNoteSearchFilterUiState) {
        _filtersUiState.value = filterUiState

        val filters: MutableList<Filters> = mutableListOf()
        val appliedCategories = filterUiState.categoryFilters.filter { it.isApplied() }
        if (appliedCategories.isNotEmpty()) {
            appliedCategories.map {
                filters.add(it)
            }
        }

        if (filterUiState.purchaseNameFilter.isApplied()) {
            filters.add(filterUiState.purchaseNameFilter)
        }

        if (filterUiState.priceFilter.isApplied()) {
            filters.add(filterUiState.priceFilter)
        }

        _appliedFilterItem.value = filters
    }

    fun resetSelectedFilters() {
        _filtersUiState.value = _filtersUiState.value.copy(
            categoryFilters = _filtersUiState.value.categoryFilters.map {
                it.copy(isSelected = false)
            },
            purchaseNameFilter = Filters.PurchaseName(),
            priceFilter = _filtersUiState.value.priceFilter.copy(
                minPrice = null,
                maxPrice = null
            )
        )

        _appliedFilterItem.value = emptyList()
    }

    private fun <T: Filters> updateAppliedFilters(filter: T, filterClass: KClass<T>) {
        val filters = _appliedFilterItem.value.toMutableList()

        val existingFilterIndex = filters.indexOfFirst {
            if (filter is Filters.Category) {
                (it is Filters.Category) && (it.categoryItem.categoryName == filter.categoryItem.categoryName)
            } else {
                filterClass.isInstance(it)
            }
        }

        if (existingFilterIndex != -1) {
            if (filter !is Filters.Category && filter.isApplied()) {
                filters[existingFilterIndex] = filter
            } else {
                filters.removeAt(existingFilterIndex)
            }
        } else {
            filters.add(filter)
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
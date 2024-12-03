package com.kjh.mynote.ui.features.purchase.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.Result
import com.example.domain.model.SortType
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.example.domain.usecase.GetPurchaseNoteSearchFilterInfoUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

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

sealed class PurchaseNoteSearchResultItem {
    data class DateItem(val date: LocalDate): PurchaseNoteSearchResultItem()
    data class ResultItem(val purchaseNoteItem: PurchaseNoteUiModel): PurchaseNoteSearchResultItem()
}

data class PurchaseNoteSearchUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val errorMsg: String? = null,
    val resultItems: List<PurchaseNoteSearchResultItem> = emptyList()
)

data class PurchaseNoteSearchFilterUiState(
    val purchaseNameFilter: Filters.PurchaseName = Filters.PurchaseName(),
    val categoryFilters: List<Filters.Category> = emptyList(),
    val dateRangeFilter: Filters.DateRange = Filters.DateRange(),
    val priceFilter: Filters.Price = Filters.Price(),
    val sortType: SortType = SortType.LATEST
)

@HiltViewModel
class PurchaseNoteSearchViewModel @Inject constructor(
    private val getPurchaseNoteSearchFilterInfoUseCase: GetPurchaseNoteSearchFilterInfoUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<PurchaseNoteSearchUiState>(PurchaseNoteSearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _filtersUiState = MutableStateFlow(PurchaseNoteSearchFilterUiState())
    val filtersUiState = _filtersUiState.asStateFlow()

    private val _resultTotalCount = MutableStateFlow(0)
    val resultTotalCount = _resultTotalCount.asStateFlow()

    var shouldScrollToTop: Boolean = false

    init {
        initializeFilters()
        observeFiltersAndFetchResults()
    }

    val appliedFiltersFlow: StateFlow<List<Filters>> =
        _filtersUiState.flatMapLatest { filterUiState ->
            val filters: MutableList<Filters> = mutableListOf()

            filterUiState.categoryFilters.filter { it.isApplied() }
                .map { filters.add(it) }

            if (filterUiState.purchaseNameFilter.isApplied()) {
                filters.add(filterUiState.purchaseNameFilter)
            }

            if (filterUiState.priceFilter.isApplied()) {
                filters.add(filterUiState.priceFilter)
            }

            flowOf(filters)
        }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private fun initializeFilters() {
        viewModelScope.launch {
            getPurchaseNoteSearchFilterInfoUseCase()
                .collectLatest { (allCategories, maxPrice) ->
                    _filtersUiState.value = PurchaseNoteSearchFilterUiState(
                        categoryFilters = allCategories.map { category ->
                            Filters.Category(
                                categoryItem = category.toUiModel()
                            )
                        },
                        priceFilter = Filters.Price(
                            myMaxPrice = maxPrice?: AppConstants.PRICE_MAX_LIMIT
                        )
                    )
                }
        }
    }

    private fun observeFiltersAndFetchResults() {
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
                    categoryIds = categoryIds,
                    sortType = filterUiState.sortType
                ).map { result -> result to filterUiState }
            }.collectLatest { (result, filterUiState) ->

                shouldScrollToTop = true
                Timber.tag("abc123").e("observeFiltersAndFetchResults() 1111")
                makeSearchResultUiItems(result, filterUiState)
            }
        }
    }

    private fun makeSearchResultUiItems(
        result: Result<List<FilteredSearchPurchaseNotes>>,
        filterUiState: PurchaseNoteSearchFilterUiState
    ) {
        when (result) {
            is Result.Loading -> {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        isEmpty = false,
                        errorMsg = null
                    )
                }
            }
            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = true,
                        errorMsg = result.msg ?: "구매노트 목록 검색에 실패하였습니다.",
                        resultItems = emptyList()
                    )
                }
            }
            is Result.Success -> {
                val resultItems = result.data?.toUiModel() ?: emptyList()
                _resultTotalCount.value = resultItems.sumOf { it.purchaseNoteItems.size }

                when {
                    resultItems.isEmpty() -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isEmpty = true,
                                resultItems = emptyList()
                            )
                        }
                    }
                    filterUiState.sortType in listOf(SortType.HIGH_PRICE, SortType.LOW_PRICE) -> {
                        _uiState.update { uiState ->
                            uiState.copy(
                                isLoading = false,
                                isEmpty = false,
                                resultItems = resultItems.flatMap { model ->
                                    model.purchaseNoteItems.map {
                                        listOf(
                                            PurchaseNoteSearchResultItem.DateItem(it.purchaseLocalDate)
                                        ) + PurchaseNoteSearchResultItem.ResultItem(it)
                                    }.flatten()
                                }
                            )
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isEmpty = false,
                                resultItems = resultItems.flatMap { model ->
                                    listOf(PurchaseNoteSearchResultItem.DateItem(model.date!!)) +
                                            model.purchaseNoteItems.map {
                                                PurchaseNoteSearchResultItem.ResultItem(it)
                                            }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun setSortType(type: SortType) {
        _filtersUiState.update {
            it.copy(sortType = type)
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
    }

    fun setPriceFilter(priceFilter: Filters.Price) {
        _filtersUiState.update {
            it.copy(priceFilter = priceFilter)
        }
    }

    fun deleteFilter(filters: Filters) {
        when (filters) {
            is Filters.Category -> {
                addOrDeleteCategoryItemBy(filters.categoryItem.id)
            }
            is Filters.Price -> {
                setPriceFilter(Filters.Price(myMaxPrice = filters.myMaxPrice))
            }
            is Filters.PurchaseName -> {
                setPurchaseName(Filters.PurchaseName())
            }
            else -> {}
        }
    }

    fun applyAllFilters(filterUiState: PurchaseNoteSearchFilterUiState) {
        _filtersUiState.value = filterUiState
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
    }

    fun shownErrorMsg() {
        _uiState.update {
            it.copy(errorMsg = null)
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
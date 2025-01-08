package com.kjh.mynote.ui.features.purchase.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PurchaseNote
import com.example.domain.model.SortType
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.example.domain.usecase.GetMaxPurchasePriceUseCase
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.Filters
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.copyIfNeeded
import com.kjh.mynote.model.getAppliedCategoryIds
import com.kjh.mynote.model.getAppliedPaymentMethodIds
import com.kjh.mynote.model.matchesFilter
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.common.uistate.PurchaseNotesUiState
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

@HiltViewModel
class PurchaseNoteSearchViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getMaxPurchasePriceUseCase: GetMaxPurchasePriceUseCase,
    private val getAllPaymentMethodUseCase: GetPaymentMethodsUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
) : ViewModel() {

    private val _filterUiState = MutableStateFlow<FilterUiState>(FilterUiState.Loading)
    val filterUiState = _filterUiState.asStateFlow()

    private val _appliedFilterItems = MutableStateFlow<List<Filters>>(emptyList())
    val appliedFilterItems = _appliedFilterItems.asStateFlow()

    var shouldNotesScrollToTop: Boolean = false
    var shouldSelectedFilterScrollToEnd: Boolean = false

    fun getFilterUiState() {
        viewModelScope.launch {
            combine(
                getAllCategoriesUseCase(),
                getAllPaymentMethodUseCase(),
                getMaxPurchasePriceUseCase()
            ) { categoriesResult, paymentMethodsResult, maxPriceResult ->
                handleFilterResults(categoriesResult, paymentMethodsResult, maxPriceResult)
            }.collectLatest {
                _filterUiState.value = it
            }
        }
    }

    val filteredPurchaseNotesUiState: StateFlow<FilteredPurchaseNotesUiState> =
        filteredPurchaseNotesUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FilteredPurchaseNotesUiState.Loading
            )

    fun setSortType(type: SortType) {
        updateFilterState { it.copy(sortType = type) }
    }

    fun setDateRangeFilter(dateRangeFilter: Filters.DateRange) {
        updateFilterState { it.copy(dateRangeFilter = dateRangeFilter) }
    }

    fun addOrDeleteCategoryFilter(filter: Filters.Category) {
        updateFilterState {
            it.copy(
                categoryFilters = it.categoryFilters.map { currentCategoryItem ->
                    if (currentCategoryItem.categoryItem.id == filter.categoryItem.id) {
                        currentCategoryItem.copy(isSelected = !currentCategoryItem.isSelected)
                    } else {
                        currentCategoryItem
                    }
                }
            )
        }
        updateSelectedFilters(filter)
    }

    fun addOrDeletePaymentMethodFilter(filter: Filters.PaymentMethod) {
        updateFilterState {
            it.copy(
                paymentMethodFilters = it.paymentMethodFilters.map { currentPaymentItem ->
                    if (currentPaymentItem.paymentMethod.paymentMethodId == filter.paymentMethod.paymentMethodId) {
                        currentPaymentItem.copy(isSelected = !currentPaymentItem.isSelected)
                    } else {
                        currentPaymentItem
                    }
                })
        }

        updateSelectedFilters(filter)
    }

    fun setPurchaseName(filter: Filters.PurchaseName) {
        updateFilterState { it.copy(purchaseNameFilter = filter) }
        updateSelectedFilters(filter)
    }

    fun setPriceFilter(priceFilter: Filters.Price) {
        updateFilterState { it.copy(priceFilter = priceFilter) }
        updateSelectedFilters(priceFilter)
    }

    private fun updateFilterState(
        transform: (PurchaseNoteFilters) -> PurchaseNoteFilters
    ) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        _filterUiState.value = filterUiState.copy(filters = transform(filterUiState.filters))
    }

    fun deleteFilter(filters: Filters) {
        when (filters) {
            is Filters.Category -> {
                addOrDeleteCategoryFilter(filters)
            }
            is Filters.PaymentMethod -> {
                addOrDeletePaymentMethodFilter(filters)
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

    fun applyAllFilters(filter: PurchaseNoteFilters) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        _filterUiState.value = filterUiState.copy(
            filters = filter
        )

        _appliedFilterItems.update { currentFilters ->
            val updatedFilters = mutableListOf<Filters>().apply {
                addAll(filter.categoryFilters.filter { it.isApplied() })
                addAll(filter.paymentMethodFilters.filter { it.isApplied() })
                if (filter.purchaseNameFilter.isApplied()) {
                    add(filter.purchaseNameFilter)
                }
                if (filter.priceFilter.isApplied()) {
                    add(filter.priceFilter)
                }
            }

            shouldSelectedFilterScrollToEnd = currentFilters.size < updatedFilters.size
            updatedFilters
        }
    }

    fun resetSelectedFilters() {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        _filterUiState.value = filterUiState.copy(
            filters = filterUiState.filters.copy(
                categoryFilters = filterUiState.filters.categoryFilters.map {
                    it.copy(isSelected = false)
                },
                paymentMethodFilters = filterUiState.filters.paymentMethodFilters.map {
                    it.copy(isSelected = false)
                },
                purchaseNameFilter = Filters.PurchaseName(),
                priceFilter = filterUiState.filters.priceFilter.copy(
                    minPrice = null,
                    maxPrice = null
                )
            )
        )
        _appliedFilterItems.value = emptyList()
    }

    private fun updateSelectedFilters(filter: Filters) {
        _appliedFilterItems.update { currentFilters ->
            val updatedFilters = currentFilters.toMutableList()

            when (filter) {
                is Filters.PurchaseName -> {
                    updatedFilters.removeAll { it.matchesFilter(filter) }
                    if (filter.isApplied()) {
                        updatedFilters.add(filter)
                    }
                }
                is Filters.Price -> {
                    updatedFilters.removeAll { it.matchesFilter(filter) }
                    if (filter.isApplied()) {
                        updatedFilters.add(filter)
                    }
                }
                is Filters.Category -> {
                    if (filter.isSelected) {
                        updatedFilters.removeAll {
                            it.matchesFilter(filter)
                        }
                    } else {
                        updatedFilters.add(filter.copy(isSelected = true))
                    }
                }
                is Filters.PaymentMethod -> {
                    if (filter.isSelected) {
                        updatedFilters.removeAll {
                            it.matchesFilter(filter)
                        }
                    } else {
                        updatedFilters.add(filter.copy(isSelected = true))
                    }
                }
                else -> {}
            }

            shouldSelectedFilterScrollToEnd = currentFilters.size < updatedFilters.size
            updatedFilters
        }
    }

    private fun getStartDateAndEndDateTimeMillis(monthFilter: DateRangeFilter?): Pair<Long, Long> {
        val now = LocalDate.now()
        return when (monthFilter) {
            is DateRangeFilter.Monthly -> {
                monthFilter.date.getFirstDayOfMonth().toMillis() to
                        monthFilter.date.getLastDayOfMonth().toMillis()
            }
            is DateRangeFilter.MonthOne -> {
                now.minusMonths(1).toMillis() to now.toMillis()
            }
            is DateRangeFilter.MonthThree -> {
                now.minusMonths(3).toMillis() to now.toMillis()
            }
            is DateRangeFilter.Directly -> {
                monthFilter.startDate.toMillis() to monthFilter.endDate.toMillis()
            }
            else -> {
                now.minusYears(1).toMillis() to now.toMillis()
            }
        }
    }

    private fun filteredPurchaseNotesUiState(): Flow<FilteredPurchaseNotesUiState> {
        return _filterUiState.flatMapLatest { filterUiState ->
            if (filterUiState !is FilterUiState.Success) {
                return@flatMapLatest flowOf(FilteredPurchaseNotesUiState.PurchaseNotes(listOf(PurchaseNotesUiState.Empty)))
            }

            val filters = filterUiState.filters
            val (startDate, endDate) = getStartDateAndEndDateTimeMillis(filters.dateRangeFilter.dateRangeFilter)
            val categoryIds = _appliedFilterItems.value.getAppliedCategoryIds()
            val paymentMethodIds = _appliedFilterItems.value.getAppliedPaymentMethodIds()

            getFilteredSearchPurchaseNotesUseCase(
                queryText = filters.purchaseNameFilter.purchaseName,
                startDate = startDate,
                endDate = endDate,
                minPrice = filters.priceFilter.minPrice ?: AppConstants.PRICE_MIN_LIMIT,
                maxPrice = filters.priceFilter.maxPrice ?: filters.priceFilter.myMaxPrice,
                categoryIds = categoryIds,
                paymentMethodIds = paymentMethodIds,
                sortType = filters.sortType
            ).map { result ->
                handlePurchaseNotesResult(result, filters)
            }
        }
    }

    private fun handlePurchaseNotesResult(
        result: ApiResult<List<PurchaseNote>>,
        filters: PurchaseNoteFilters,
    ): FilteredPurchaseNotesUiState {
        return when (result) {
            is ApiResult.Loading -> {
                FilteredPurchaseNotesUiState.Loading
            }
            is ApiResult.Error -> {
                FilteredPurchaseNotesUiState.Error("구매노트 목록을 불러오는데 실패하였습니다.")
            }
            is ApiResult.Success -> {
                shouldNotesScrollToTop = true

                val resultItems = result.data.toUiModel()
                val totalCount = resultItems.size

                if (resultItems.isEmpty()) {
                    return FilteredPurchaseNotesUiState.PurchaseNotes(listOf(PurchaseNotesUiState.Empty))
                }

                val items =
                    if (filters.sortType in listOf(SortType.HIGH_PRICE, SortType.LOW_PRICE)) {
                        resultItems.flatMap { item ->
                            listOf(PurchaseNotesUiState.DateItem(item.localDate)) + PurchaseNotesUiState.PurchaseNoteItem(
                                item
                            )
                        }
                    } else {
                        resultItems.groupBy { it.localDate }
                            .flatMap { (date, items) ->
                                listOf(PurchaseNotesUiState.DateItem(date)) + items.map {
                                    PurchaseNotesUiState.PurchaseNoteItem(it)
                                }
                            }
                    }

                FilteredPurchaseNotesUiState.PurchaseNotes(items, totalCount)
            }
        }
    }

    private fun handleFilterResults(
        categoriesResult: ApiResult<List<Category>>,
        paymentMethodsResult: ApiResult<List<PaymentMethod>>,
        maxPriceResult: ApiResult<Long?>
    ): FilterUiState {
        return when {
            categoriesResult is ApiResult.Loading
                    || paymentMethodsResult is ApiResult.Loading
                    || maxPriceResult is ApiResult.Loading -> {
                FilterUiState.Loading
            }
            categoriesResult is ApiResult.Error -> {
                FilterUiState.Error("카테고리 목록을 불러오는데 실패하였습니다.")
            }
            paymentMethodsResult is ApiResult.Error -> {
                FilterUiState.Error("결제수단 목록을 불러오는데 실패하였습니다.")
            }
            maxPriceResult is ApiResult.Error -> {
                FilterUiState.Error("최대 금액 정보를 불러오는데 실패하였습니다.")
            }
            categoriesResult is ApiResult.Success
                    && paymentMethodsResult is ApiResult.Success
                    && maxPriceResult is ApiResult.Success -> {

                val purchaseNoteFilterItem = makePurchaseNoteFilters(
                    categories = categoriesResult.data.toUiModel(),
                    paymentMethods = paymentMethodsResult.data.toUiModel(),
                    maxPrice = maxPriceResult.data ?: AppConstants.PRICE_MAX_LIMIT
                )

                FilterUiState.Success(purchaseNoteFilterItem)
            }

            else -> FilterUiState.Error("구매노트 검색 정보를 불러오는데 실패하였습니다.")
        }
    }

    private fun makePurchaseNoteFilters(
        categories: List<CategoryUiModel>,
        paymentMethods: List<PaymentMethodUiModel>,
        maxPrice: Long
    ): PurchaseNoteFilters {
        val currentFilter = (_filterUiState.value as? FilterUiState.Success)?.filters ?: PurchaseNoteFilters()

        return PurchaseNoteFilters(
            dateRangeFilter = currentFilter.dateRangeFilter,
            categoryFilters = categories.map { category ->
                Filters.Category(
                    categoryItem = category,
                    isSelected = category.id in
                            currentFilter.categoryFilters.getAppliedCategoryIds().toSet()
                )
            },
            paymentMethodFilters = paymentMethods.map { paymentMethod ->
                Filters.PaymentMethod(
                    paymentMethod = paymentMethod,
                    isSelected = paymentMethod.paymentMethodId in
                            currentFilter.paymentMethodFilters.getAppliedPaymentMethodIds().toSet()
                )
            },
            purchaseNameFilter = currentFilter.purchaseNameFilter,
            priceFilter = currentFilter.priceFilter.copy(myMaxPrice = maxPrice),
            sortType = currentFilter.sortType
        )
    }
}

data class PurchaseNoteFilters(
    val purchaseNameFilter: Filters.PurchaseName = Filters.PurchaseName(),
    val categoryFilters: List<Filters.Category> = emptyList(),
    val paymentMethodFilters: List<Filters.PaymentMethod> = emptyList(),
    val dateRangeFilter: Filters.DateRange = Filters.DateRange(),
    val priceFilter: Filters.Price = Filters.Price(),
    val sortType: SortType = SortType.LATEST,
)

sealed interface FilterUiState {
    data object Loading : FilterUiState
    data class Error(val errorMsg: String) : FilterUiState
    data class Success(val filters: PurchaseNoteFilters) : FilterUiState
}

sealed interface FilteredPurchaseNotesUiState {
    data object Loading : FilteredPurchaseNotesUiState
    data class Error(val errorMsg: String) : FilteredPurchaseNotesUiState
    data class PurchaseNotes(
        val items: List<PurchaseNotesUiState>,
        val totalCount: Int = 0
    ) : FilteredPurchaseNotesUiState
}
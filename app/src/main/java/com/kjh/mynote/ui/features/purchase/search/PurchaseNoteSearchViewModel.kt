package com.kjh.mynote.ui.features.purchase.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchaseNoteFilters
import com.example.domain.model.SortType
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.example.domain.usecase.GetPurchaseNoteSearchFiltersUseCase
import com.kjh.mynote.model.Filters
import com.kjh.mynote.model.getAppliedCategoryIds
import com.kjh.mynote.model.getAppliedPaymentMethodIds
import com.kjh.mynote.model.matchesFilter
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.common.uistate.PurchaseNotesUiState
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 21..
 * Description:
 */

data class PurchaseNoteFiltersUiState(
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val dateRangeFilter: Filters.DateRange = Filters.DateRange(),
    val categoryFilters: List<Filters.Category> = emptyList(),
    val paymentMethodFilters: List<Filters.PaymentMethod> = emptyList(),
    val purchaseNameFilter: Filters.PurchaseName = Filters.PurchaseName(),
    val priceFilter: Filters.Price = Filters.Price(),
    val sortType: SortType = SortType.LATEST
)

@HiltViewModel
class PurchaseNoteSearchViewModel @Inject constructor(
    private val getPurchaseNoteSearchFiltersUseCase: GetPurchaseNoteSearchFiltersUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
) : ViewModel() {

    private val _filterUiState = MutableStateFlow(PurchaseNoteFiltersUiState())
    val filterUiState = _filterUiState.asStateFlow()

    private val _appliedFilterItems = MutableStateFlow<List<Filters>>(emptyList())
    val appliedFilterItems = _appliedFilterItems.asStateFlow()

    var shouldNotesScrollToTop: Boolean = false
    var shouldSelectedFilterScrollToEnd: Boolean = false

    fun getFilterUiState() {
        viewModelScope.launch {
            getPurchaseNoteSearchFiltersUseCase().collect { result ->
                handlePurchaseNoteSearchFiltersResult(result)
            }
        }
    }

    val filteredPurchaseNotesUiState: StateFlow<FilteredPurchaseNotesUiState> = _filterUiState
        .flatMapLatest { filterUiState ->
            val (startDate, endDate) = filterUiState.dateRangeFilter.dateRangeFilter.getDateRange()

            getFilteredSearchPurchaseNotesUseCase(
                queryText = filterUiState.purchaseNameFilter.purchaseName,
                startDate = startDate.toMillis(),
                endDate = endDate.toMillis(),
                minPrice = filterUiState.priceFilter.minPrice,
                maxPrice = filterUiState.priceFilter.maxPrice
                    ?: filterUiState.priceFilter.myMaxPrice,
                categoryIds = filterUiState.categoryFilters.getAppliedCategoryIds(),
                paymentMethodIds = filterUiState.paymentMethodFilters.getAppliedPaymentMethodIds(),
                sortType = filterUiState.sortType
            ).map { result ->
                handleFilteredSearchPurchaseNotesResult(filterUiState.sortType, result)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FilteredPurchaseNotesUiState.Loading
        )

    fun setSortType(type: SortType) {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(sortType = type)
        }
    }

    fun setDateRangeFilter(dateRangeFilter: Filters.DateRange) {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(dateRangeFilter = dateRangeFilter)
        }
    }

    fun addOrDeleteCategoryFilter(filter: Filters.Category) {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(
                categoryFilters = filterUiState.categoryFilters.map { currentCategory ->
                    if (currentCategory.categoryItem.id == filter.categoryItem.id) {
                        currentCategory.copy(isSelected = !currentCategory.isSelected)
                    } else {
                        currentCategory
                    }
                }
            )
        }
        updateSelectedFilters(filter)
    }

    fun addOrDeletePaymentMethodFilter(filter: Filters.PaymentMethod) {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(
                paymentMethodFilters = filterUiState.paymentMethodFilters.map { currentPaymentMethod ->
                    if (currentPaymentMethod.paymentMethod.paymentMethodId == filter.paymentMethod.paymentMethodId) {
                        currentPaymentMethod.copy(isSelected = !currentPaymentMethod.isSelected)
                    } else {
                        currentPaymentMethod
                    }
                }
            )
        }
        updateSelectedFilters(filter)
    }

    fun setPurchaseName(filter: Filters.PurchaseName) {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(purchaseNameFilter = filter)
        }
        updateSelectedFilters(filter)
    }

    fun setPriceFilter(priceFilter: Filters.Price) {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(priceFilter = priceFilter)
        }
        updateSelectedFilters(priceFilter)
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

    fun applyAllFilters(filterUiState: PurchaseNoteFiltersUiState) {
        _filterUiState.value = filterUiState

        _appliedFilterItems.update { currentFilters ->
            val updatedFilters = mutableListOf<Filters>().apply {
                addAll(filterUiState.categoryFilters.filter { it.isApplied() })
                addAll(filterUiState.paymentMethodFilters.filter { it.isApplied() })
                if (filterUiState.purchaseNameFilter.isApplied()) {
                    add(filterUiState.purchaseNameFilter)
                }
                if (filterUiState.priceFilter.isApplied()) {
                    add(filterUiState.priceFilter)
                }
            }

            shouldSelectedFilterScrollToEnd = currentFilters.size < updatedFilters.size
            updatedFilters
        }
    }

    fun resetSelectedFilters() {
        _filterUiState.update { filterUiState ->
            filterUiState.copy(
                categoryFilters = filterUiState.categoryFilters.map {
                    it.copy(isSelected = false)
                },
                paymentMethodFilters = filterUiState.paymentMethodFilters.map {
                    it.copy(isSelected = false)
                },
                purchaseNameFilter = Filters.PurchaseName(),
                priceFilter = filterUiState.priceFilter.copy(
                    minPrice = null,
                    maxPrice = null
                )
            )
        }
        _appliedFilterItems.value = emptyList()
    }

    private fun updateSelectedFilters(filter: Filters) {
        _appliedFilterItems.update { currentFilters ->
            val updatedFilters = currentFilters.toMutableList()

            when (filter) {
                is Filters.PurchaseName,
                is Filters.Price -> {
                    updatedFilters.removeAll { it.matchesFilter(filter) }
                    if (filter.isApplied()) {
                        updatedFilters.add(filter)
                    }
                }
                is Filters.Category -> {
                    if (filter.isSelected) {
                        updatedFilters.removeAll { it.matchesFilter(filter) }
                    } else {
                        updatedFilters.add(filter.copy(isSelected = true))
                    }
                }
                is Filters.PaymentMethod -> {
                    if (filter.isSelected) {
                        updatedFilters.removeAll { it.matchesFilter(filter) }
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

    private fun handlePurchaseNoteSearchFiltersResult(result: ApiResult<PurchaseNoteFilters>) {
        when (result) {
            is ApiResult.Loading -> {
                _filterUiState.update {
                    it.copy(isLoading = true)
                }
            }
            is ApiResult.Error -> {
                _filterUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.error.message ?: "필터 목록을 불러오는데 실패하였습니다."
                    )
                }
            }
            is ApiResult.Success -> {
                val (categories, paymentMethods, highestPrice) = result.data
                val appliedFilterItems = _appliedFilterItems.value

                _filterUiState.update { uiState ->
                    val categoryFilters = categories.map { category ->
                        Filters.Category(
                            categoryItem = category.toUiModel(),
                            isSelected = category.id in appliedFilterItems.getAppliedCategoryIds()
                                .toSet()
                        )
                    }
                    val paymentMethodFilters = paymentMethods.map { paymentMethod ->
                        Filters.PaymentMethod(
                            paymentMethod = paymentMethod.toUiModel(),
                            isSelected = paymentMethod.paymentMethodId in
                                    appliedFilterItems.getAppliedPaymentMethodIds().toSet()
                        )
                    }
                    val priceFilter = uiState.priceFilter.copy(
                        myMaxPrice = highestPrice ?: AppConstants.PRICE_MAX_LIMIT
                    )

                    uiState.copy(
                        isLoading = false,
                        categoryFilters = categoryFilters,
                        paymentMethodFilters = paymentMethodFilters,
                        priceFilter = priceFilter
                    )
                }
            }
        }
    }

    private fun handleFilteredSearchPurchaseNotesResult(
        currentSortType: SortType,
        result: ApiResult<List<PurchaseNote>>
    ): FilteredPurchaseNotesUiState {
        return when (result) {
            is ApiResult.Loading -> {
                FilteredPurchaseNotesUiState.Loading
            }
            is ApiResult.Error -> {
                val errorMsg = result.error.message ?: "구매노트 목록을 불러오는데 실패하였습니다."
                FilteredPurchaseNotesUiState.Error(errorMsg)
            }
            is ApiResult.Success -> {
                val purchaseNotes = result.data.toUiModel()
                val totalCount = purchaseNotes.size

                val uiItems = if (purchaseNotes.isEmpty()) {
                    listOf(PurchaseNotesUiState.Empty)
                } else if (currentSortType in listOf(SortType.HIGH_PRICE, SortType.LOW_PRICE)) {
                    purchaseNotes.flatMap { item ->
                        listOf(PurchaseNotesUiState.DateItem(item.localDate)) + PurchaseNotesUiState.PurchaseNoteItem(
                            item
                        )
                    }
                } else {
                    purchaseNotes.groupBy { it.localDate }
                        .flatMap { (date, items) ->
                            listOf(PurchaseNotesUiState.DateItem(date)) + items.map {
                                PurchaseNotesUiState.PurchaseNoteItem(it)
                            }
                        }
                }

                shouldNotesScrollToTop = true
                FilteredPurchaseNotesUiState.PurchaseNotes(uiItems, totalCount)
            }
        }
    }
}

sealed interface FilteredPurchaseNotesUiState {
    data object Loading : FilteredPurchaseNotesUiState
    data class Error(val errorMsg: String) : FilteredPurchaseNotesUiState
    data class PurchaseNotes(
        val items: List<PurchaseNotesUiState>,
        val totalCount: Int = 0
    ) : FilteredPurchaseNotesUiState
}
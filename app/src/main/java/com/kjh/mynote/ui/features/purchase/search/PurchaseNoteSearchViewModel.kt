package com.kjh.mynote.ui.features.purchase.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.PaymentMethod
import com.example.domain.model.SortType
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.example.domain.usecase.GetFilteredSearchPurchaseNotesUseCase
import com.example.domain.usecase.GetMaxPurchasePriceUseCase
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.common.uistate.PurchaseNotesUiState
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.launch
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
        val purchaseName: String = "",
    ) : Filters() {
        override fun isApplied(): Boolean = purchaseName.isNotBlank()
    }

    data class Category(
        val categoryItem: CategoryUiModel,
        val isSelected: Boolean = false,
    ) : Filters() {
        override fun isApplied(): Boolean = isSelected
    }

    data class PaymentMethod(
        val paymentMethod: PaymentMethodUiModel,
        val isSelected: Boolean = false,
    ) : Filters() {
        override fun isApplied(): Boolean = isSelected
    }

    data class DateRange(
        val dateRangeFilter: DateRangeFilter = DateRangeFilter.Monthly(),
    ) : Filters() {
        override fun isApplied(): Boolean = true
    }

    data class Price(
        val minPrice: Long? = null,
        val maxPrice: Long? = null,
        val myMaxPrice: Long = AppConstants.PRICE_MAX_LIMIT,
    ) : Filters() {
        override fun isApplied(): Boolean =
            minPrice != null || maxPrice != null
    }
}

@HiltViewModel
class PurchaseNoteSearchViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getMaxPurchasePriceUseCase: GetMaxPurchasePriceUseCase,
    private val getAllPaymentMethodUseCase: GetPaymentMethodsUseCase,
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
) : ViewModel() {

    private val _filterUiState = MutableStateFlow<FilterUiState>(FilterUiState.Loading)
    val filterUiState = _filterUiState.asStateFlow()

    var shouldScrollToTop: Boolean = false

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

    val appliedFilterItems: StateFlow<List<Filters>> =
        getAppliedFilters()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setSortType(type: SortType) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        val updateFilterState = filterUiState.copy(
            filters = filterUiState.filters.copy(
                sortType = type
            )
        )

        _filterUiState.value = updateFilterState
    }

    fun setDateRangeFilter(dateRangeFilter: Filters.DateRange) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        val updatedFilterState = filterUiState.copy(
            filters = filterUiState.filters.copy(
                dateRangeFilter = dateRangeFilter
            )
        )

        _filterUiState.value = updatedFilterState
    }

    fun addOrDeleteCategoryItemBy(categoryId: Int) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        val updatedCategoryItem =
            filterUiState.filters.categoryFilters.map { currentCategoryItem ->
                if (currentCategoryItem.categoryItem.id == categoryId) {
                    currentCategoryItem.copy(
                        isSelected = !currentCategoryItem.isSelected
                    )
                } else {
                    currentCategoryItem
                }
            }

        _filterUiState.value = filterUiState.copy(
            filters = filterUiState.filters.copy(
                categoryFilters = updatedCategoryItem
            )
        )
    }

    fun addOrDeletePaymentMethodItemBy(paymentMethodId: Int) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        val updatePaymentItem =
            filterUiState.filters.paymentMethodFilters.map { currentPaymentItem ->
                if (currentPaymentItem.paymentMethod.paymentMethodId == paymentMethodId) {
                    currentPaymentItem.copy(
                        isSelected = !currentPaymentItem.isSelected
                    )
                } else {
                    currentPaymentItem
                }
            }

        _filterUiState.value = filterUiState.copy(
            filters = filterUiState.filters.copy(
                paymentMethodFilters = updatePaymentItem
            )
        )
    }

    fun setPurchaseName(filter: Filters.PurchaseName) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        val updatedFilterState = filterUiState.copy(
            filters = filterUiState.filters.copy(
                purchaseNameFilter = filter
            )
        )
        _filterUiState.value = updatedFilterState
    }

    fun setPriceFilter(priceFilter: Filters.Price) {
        val filterUiState = _filterUiState.value as? FilterUiState.Success ?: return
        val updatedFilterState = filterUiState.copy(
            filters = filterUiState.filters.copy(
                priceFilter = priceFilter
            )
        )
        _filterUiState.value = updatedFilterState
    }

    fun deleteFilter(filters: Filters) {
        when (filters) {
            is Filters.Category -> {
                addOrDeleteCategoryItemBy(filters.categoryItem.id)
            }
            is Filters.PaymentMethod -> {
                addOrDeletePaymentMethodItemBy(filters.paymentMethod.paymentMethodId)
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

    private fun getAppliedFilters(): Flow<List<Filters>> {
        return _filterUiState.flatMapLatest { filterUiState ->
            val filters: MutableList<Filters> = mutableListOf()

            if (filterUiState !is FilterUiState.Success) {
                return@flatMapLatest flowOf(filters)
            }

            filterUiState.filters.categoryFilters.filter { it.isApplied() }
                .forEach { filters.add(it) }

            filterUiState.filters.paymentMethodFilters.filter { it.isApplied() }
                .forEach { filters.add(it) }

            if (filterUiState.filters.purchaseNameFilter.isApplied()) {
                filters.add(filterUiState.filters.purchaseNameFilter)
            }

            if (filterUiState.filters.priceFilter.isApplied()) {
                filters.add(filterUiState.filters.priceFilter)
            }

            flowOf(filters)
        }
    }

    private fun filteredPurchaseNotesUiState(): Flow<FilteredPurchaseNotesUiState> {
        return _filterUiState.flatMapLatest { filterUiState ->
            if (filterUiState !is FilterUiState.Success) {
                return@flatMapLatest flowOf(FilteredPurchaseNotesUiState.Empty)
            }

            val filters = filterUiState.filters
            val (startDate, endDate) = getStartDateAndEndDateTimeMillis(filters.dateRangeFilter.dateRangeFilter)
            val categoryIds = filters.categoryFilters.filter { it.isApplied() }
                .map { it.categoryItem.id }
            val paymentMethodIds = filters.paymentMethodFilters.filter { it.isApplied() }
                .map { it.paymentMethod.paymentMethodId }

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
        result: ApiResult<List<FilteredSearchPurchaseNotes>>,
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
                shouldScrollToTop = true

                val resultItems = result.data.toUiModel()
                val totalCount = resultItems.sumOf { it.purchaseNoteItems.size }

                if (resultItems.isEmpty()) {
                    FilteredPurchaseNotesUiState.Empty
                }

                val items = if (filters.sortType in listOf(SortType.HIGH_PRICE, SortType.LOW_PRICE)) {
                    resultItems.flatMap { model ->
                        model.purchaseNoteItems.map {
                            listOf(PurchaseNotesUiState.DateItem(it.purchaseLocalDate)) + PurchaseNotesUiState.PurchaseNoteItem(it)
                        }.flatten()
                    }
                } else {
                    resultItems.flatMap { model ->
                        listOf(PurchaseNotesUiState.DateItem(model.date!!)) + model.purchaseNoteItems.map {
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

            else -> FilterUiState.Error("구매노트 검색 정볼르 불러오는데 실패하였습니다.")
        }
    }

    private fun makePurchaseNoteFilters(
        categories: List<CategoryUiModel>,
        paymentMethods: List<PaymentMethodUiModel>,
        maxPrice: Long
    ): PurchaseNoteFilters {
        val currentFilter = (_filterUiState.value as? FilterUiState.Success)?.filters ?: PurchaseNoteFilters()

        val selectedCategories = currentFilter.categoryFilters.filter { it.isSelected }
        val selectedPaymentMethods = currentFilter.paymentMethodFilters.filter { it.isSelected }

        return PurchaseNoteFilters(
            dateRangeFilter = currentFilter.dateRangeFilter,
            categoryFilters = categories.map { category ->
                Filters.Category(
                    categoryItem = category,
                    isSelected = selectedCategories.any { it.categoryItem.id == category.id }
                )
            },
            paymentMethodFilters = paymentMethods.map { paymentMethod ->
                Filters.PaymentMethod(
                    paymentMethod = paymentMethod,
                    isSelected = selectedPaymentMethods.any { it.paymentMethod.paymentMethodId == paymentMethod.paymentMethodId}
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
    data object Empty : FilteredPurchaseNotesUiState
    data class Error(val errorMsg: String) : FilteredPurchaseNotesUiState
    data class PurchaseNotes(
        val items: List<PurchaseNotesUiState>,
        val totalCount: Int = 0
    ) : FilteredPurchaseNotesUiState
}
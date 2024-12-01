package com.kjh.mynote.ui.features.purchase.search.filters.whole

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.ui.features.place.search.result.DateRangeFilter
import com.kjh.mynote.ui.features.purchase.search.Filters
import com.kjh.mynote.ui.features.purchase.search.PurchaseNoteSearchFilterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 27..
 * Description:
 */

sealed class PriceValidateEvent {
    data object Valid: PriceValidateEvent()
    data class Error(val msg: String): PriceValidateEvent()
}

@HiltViewModel
class PurchaseNoteSearchWholeFilterViewModel @Inject constructor(): ViewModel() {

    private val _initFilterUiState = MutableStateFlow(PurchaseNoteSearchFilterUiState())

    private val _tempFilterUiState = MutableStateFlow(PurchaseNoteSearchFilterUiState())
    val tempFilterUiState = _tempFilterUiState.asStateFlow()

    private val _priceValidateEventState = MutableSharedFlow<PriceValidateEvent>()
    val priceValidateEventState = _priceValidateEventState.asSharedFlow()

    val isChangedCategoryFilters: StateFlow<Boolean> = combine(
        _initFilterUiState, _tempFilterUiState
    ) { initFilter, tempFilter ->
        initFilter.categoryFilters != tempFilter.categoryFilters
    }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            false
        )

    val isChangedPriceFilter: StateFlow<Boolean> = combine(
        _initFilterUiState, _tempFilterUiState
    ) { initFilter, tempFilter ->
        initFilter.priceFilter.minPrice != tempFilter.priceFilter.minPrice
                || initFilter.priceFilter.maxPrice != tempFilter.priceFilter.maxPrice
    }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            false
        )

    val isChangedPurchaseNameFilter: StateFlow<Boolean> = combine(
        _initFilterUiState, _tempFilterUiState
    ) { initFilter, tempFilter ->
        initFilter.purchaseNameFilter.purchaseName !=
                tempFilter.purchaseNameFilter.purchaseName
    }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            false
        )

    val isChangedDateFilter: StateFlow<Boolean> = combine(
        _initFilterUiState, _tempFilterUiState
    ) { initFilter, tempFilter ->
        initFilter.dateRangeFilter != tempFilter.dateRangeFilter
    }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            false
        )

    val isChangedFilters = combine(
        isChangedCategoryFilters,
        isChangedPurchaseNameFilter,
        isChangedPriceFilter
    ) { changedCategory, changedPurchaseName, changedPrice ->
        changedCategory || changedPurchaseName || changedPrice
    } .stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        false
    )

    fun setInitFilterUiState(state: PurchaseNoteSearchFilterUiState) {
        _initFilterUiState.value = state
        _tempFilterUiState.value = state
    }

    fun updateCategoryFilter(categoryId: Int) {
        _tempFilterUiState.update { state ->
            state.copy(
                categoryFilters = state.categoryFilters.map { categoryFilter ->
                    if (categoryFilter.categoryItem.id == categoryId) {
                        categoryFilter.copy(
                            isSelected = !categoryFilter.isSelected
                        )
                    } else {
                        categoryFilter
                    }
                }
            )
        }
    }

    fun setDateFilter(dateFilter: DateRangeFilter) {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                dateRangeFilter = tempState.dateRangeFilter.copy(
                    dateRangeFilter = dateFilter
                )
            )
        }
    }

    fun resetDateFilter() {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                dateRangeFilter = tempState.dateRangeFilter.copy(
                    dateRangeFilter = DateRangeFilter.Monthly()
                )
            )
        }
    }

    fun setPurchaseName(targetName: String) {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                purchaseNameFilter = tempState.purchaseNameFilter.copy(
                    purchaseName = targetName
                )
            )
        }
    }

    fun clearPurchaseName() {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                purchaseNameFilter = Filters.PurchaseName()
            )
        }
    }

    fun setMinPrice(minPrice: String) {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                priceFilter = tempState.priceFilter.copy(
                    minPrice = minPrice.toLong()
                )
            )
        }
    }

    fun setMaxPrice(maxPrice: String) {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                priceFilter = tempState.priceFilter.copy(
                    maxPrice = maxPrice.toLong()
                )
            )
        }
    }

    fun clearMinPrice() {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                priceFilter = tempState.priceFilter.copy(
                    minPrice = null
                )
            )
        }
    }

    fun clearMaxPrice() {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                priceFilter = tempState.priceFilter.copy(
                    maxPrice = null
                )
            )
        }
    }

    fun resetPriceFilter() {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                priceFilter = tempState.priceFilter.copy(
                    minPrice = null,
                    maxPrice = null
                )
            )
        }
    }

    fun resetAllFilters() {
        _tempFilterUiState.update { tempState ->
            tempState.copy(
                categoryFilters = tempState.categoryFilters.map { categoryFilter ->
                    categoryFilter.copy(isSelected = false)
                },
                purchaseNameFilter = Filters.PurchaseName(),
                priceFilter = tempState.priceFilter.copy(
                    minPrice = null,
                    maxPrice = null
                )
            )
        }
    }

    fun checkPriceValidation() {
        viewModelScope.launch {
            val (minPrice, maxPrice) = _tempFilterUiState.value.priceFilter
            val isMinPriceBiggerThanMaxPrice = (minPrice != null && maxPrice != null) && (minPrice > maxPrice)
            if (isMinPriceBiggerThanMaxPrice) {
                _priceValidateEventState.emit(PriceValidateEvent.Error("최소, 최대 금액을 다시 한번 확인해주세요"))
                return@launch
            }

            _priceValidateEventState.emit(PriceValidateEvent.Valid)
        }
    }
}
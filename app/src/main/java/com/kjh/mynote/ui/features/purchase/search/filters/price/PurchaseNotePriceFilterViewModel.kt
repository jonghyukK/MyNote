package com.kjh.mynote.ui.features.purchase.search.filters.price

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.ui.features.purchase.search.PriceFilter
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 25..
 * Description:
 */

data class PurchaseNotePriceFilterUiState(
    val initPriceFilter: PriceFilter = PriceFilter(),
    val tempPriceFilter: PriceFilter = initPriceFilter
)

fun PurchaseNotePriceFilterUiState.isChangedFilter() =
    initPriceFilter.minPrice != tempPriceFilter.minPrice
            || initPriceFilter.maxPrice != tempPriceFilter.maxPrice

sealed class PriceValidateEvent {
    data object Valid: PriceValidateEvent()
    data class Error(val msg: String): PriceValidateEvent()
}

@HiltViewModel
class PurchaseNotePriceFilterViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseNotePriceFilterUiState())
    val uiState = _uiState.asStateFlow()

    private val _priceValidateEventState = MutableSharedFlow<PriceValidateEvent>()
    val priceValidateEventState = _priceValidateEventState.asSharedFlow()

    fun setInitPrices(initPriceFilter: PriceFilter) {
        _uiState.value = PurchaseNotePriceFilterUiState(initPriceFilter)
    }

    fun setTempMinPrice(min: String) {
        _uiState.update { uiState ->
            uiState.copy(
                tempPriceFilter = uiState.tempPriceFilter.copy(minPrice = min.toLong())
            )
        }
    }

    fun setTempMaxPrice(max: String) {
        _uiState.update { uiState ->
            uiState.copy(
                tempPriceFilter = uiState.tempPriceFilter.copy(maxPrice = max.toLong())
            )
        }
    }

    fun clearTempMinPrice() {
        _uiState.update { uiState ->
            uiState.copy(
                tempPriceFilter = uiState.tempPriceFilter.copy(minPrice = 0)
            )
        }
    }

    fun clearTempMaxPrice() {
        _uiState.update { uiState ->
            uiState.copy(
                tempPriceFilter = uiState.tempPriceFilter.copy(maxPrice = 0)
            )
        }
    }

    fun resetTempPrices() {
        _uiState.update { uiState ->
            uiState.copy(
                tempPriceFilter = uiState.tempPriceFilter.copy(
                    minPrice = AppConstants.PRICE_MIN_LIMIT,
                    maxPrice = uiState.initPriceFilter.myMaxPrice
                )
            )
        }
    }

    fun checkPriceValidation() {
        viewModelScope.launch {
            val isMinPriceLessOrSameThanMaxPrice =
                _uiState.value.tempPriceFilter.minPrice <= _uiState.value.tempPriceFilter.maxPrice
            val isMaxPriceBiggerThan0 = _uiState.value.tempPriceFilter.maxPrice > 0

            when {
                !isMinPriceLessOrSameThanMaxPrice -> {
                    _priceValidateEventState.emit(PriceValidateEvent.Error("최소, 최대 금액을 다시 한번 확인해주세요"))
                }
                !isMaxPriceBiggerThan0 -> {
                    _priceValidateEventState.emit(PriceValidateEvent.Error("최대 금액은 0보다 큰 금액이어야 합니다"))
                }
                else -> {
                    _priceValidateEventState.emit(PriceValidateEvent.Valid)
                }
            }
        }
    }
}

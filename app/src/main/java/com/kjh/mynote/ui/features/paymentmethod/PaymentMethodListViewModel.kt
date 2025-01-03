package com.kjh.mynote.ui.features.paymentmethod

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.asResult
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */

data class SelectablePaymentMethodItem(
    val isSelected: Boolean = false,
    val paymentMethodItem: PaymentMethodUiModel
)

sealed interface PaymentMethodListUiState {
    data object Loading: PaymentMethodListUiState
    data class Error(val errorMsg: String?): PaymentMethodListUiState
    data class PaymentMethods(val items: List<SelectablePaymentMethodItem>): PaymentMethodListUiState
}

@HiltViewModel
class PaymentMethodListViewModel @Inject constructor(
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _initPaymentMethodItem: StateFlow<PaymentMethodUiModel?> =
        savedStateHandle.getStateFlow(AppConstants.INTENT_PAYMENT_METHOD_ITEM, null)

    val uiState = combine(
        _initPaymentMethodItem, getPaymentMethodsUseCase().asResult()
    ) { initItem, paymentMethodsResult ->
        when (paymentMethodsResult) {
            is ApiResult.Loading -> PaymentMethodListUiState.Loading
            is ApiResult.Error -> {
                val errorMsg = paymentMethodsResult.error.message ?: "결제수단 목록 조회가 실패하였습니다."
                PaymentMethodListUiState.Error(errorMsg)
            }
            is ApiResult.Success -> {
                val items = paymentMethodsResult.data.map { paymentMethod ->
                    SelectablePaymentMethodItem(
                        isSelected = initItem?.paymentMethodId == paymentMethod.paymentMethodId,
                        paymentMethodItem = paymentMethod.toUiModel()
                    )
                }

                PaymentMethodListUiState.PaymentMethods(items)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaymentMethodListUiState.Loading
    )
}
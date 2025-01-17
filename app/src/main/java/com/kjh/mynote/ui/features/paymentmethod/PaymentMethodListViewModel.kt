package com.kjh.mynote.ui.features.paymentmethod

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.features.paymentmethod.PaymentMethodListBSDialog.Companion.ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */

@HiltViewModel
class PaymentMethodListViewModel @Inject constructor(
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _selectedPaymentMethodItem = MutableStateFlow<PaymentMethodUiModel?>(
        savedStateHandle[ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM]
    )
    val selectedPaymentMethodItem = _selectedPaymentMethodItem.asStateFlow()

    val uiState: StateFlow<PaymentMethodListUiState> =
        getPaymentMethodsUseCase()
            .map { result ->
                val uiState = mapResultToUiState(result)
                if (uiState is PaymentMethodListUiState.Success) {
                    updateSelectedPaymentMethodItem(uiState.items.map { it.paymentMethodItem })
                }

                uiState
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                PaymentMethodListUiState.Loading
            )

    private fun mapResultToUiState(
        result: ApiResult<List<PaymentMethod>>
    ): PaymentMethodListUiState =
        when (result) {
            is ApiResult.Loading -> {
                PaymentMethodListUiState.Loading
            }
            is ApiResult.Error -> {
                val errorMsg = result.error.message ?: "결제수단 목록 조회가 실패하였습니다."
                PaymentMethodListUiState.Error(errorMsg)
            }
            is ApiResult.Success -> {
                PaymentMethodListUiState.Success(
                    items = result.data.map { paymentMethod ->
                        SelectablePaymentMethodItem(
                            isSelected = paymentMethod.paymentMethodId ==
                                    selectedPaymentMethodItem.value?.paymentMethodId,
                            paymentMethodItem = paymentMethod.toUiModel()
                        )
                    }
                )
            }
        }

    private fun updateSelectedPaymentMethodItem(paymentMethodItems: List<PaymentMethodUiModel>) {
        _selectedPaymentMethodItem.value = paymentMethodItems
            .find { it.paymentMethodId == selectedPaymentMethodItem.value?.paymentMethodId }
    }
}

data class SelectablePaymentMethodItem(
    val isSelected: Boolean = false,
    val paymentMethodItem: PaymentMethodUiModel
)

sealed interface PaymentMethodListUiState {
    data object Loading: PaymentMethodListUiState
    data class Error(val errorMsg: String): PaymentMethodListUiState
    data class Success(val items: List<SelectablePaymentMethodItem>): PaymentMethodListUiState
}
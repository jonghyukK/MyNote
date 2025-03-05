package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.add

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.UpsertPaymentMethodUseCase
import com.kjh.mynote.ui_compose.base.BaseComposeViewModel
import com.kjh.mynote.ui_compose.base.UiEvent
import com.kjh.mynote.ui_compose.base.UiSideEffect
import com.kjh.mynote.ui_compose.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 4..
 * Description:
 */

sealed interface PaymentMethodAddSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): PaymentMethodAddSideEffect
    data object NavigateBack: PaymentMethodAddSideEffect
}

sealed interface PaymentMethodAddEvent: UiEvent {
    data object UpdateDefaultPaymentChecked: PaymentMethodAddEvent
    data class UpdatePaymentMethodName(val name: String): PaymentMethodAddEvent
    data object AddPaymentMethod: PaymentMethodAddEvent
}

data class PaymentMethodAddUiState(
    val paymentMethodName: String = "",
    val isCheckedDefaultPayment: Boolean = false,
    val isValidName: Boolean = false
): UiState

@HiltViewModel
class PaymentMethodAddViewModel @Inject constructor(
    private val upsertPaymentMethodUseCase: UpsertPaymentMethodUseCase,
): BaseComposeViewModel<PaymentMethodAddEvent, PaymentMethodAddUiState, PaymentMethodAddSideEffect>() {

    override fun createInitialState(): PaymentMethodAddUiState {
        return PaymentMethodAddUiState()
    }

    override fun handleEvent(event: PaymentMethodAddEvent) {
        when (event) {
            is PaymentMethodAddEvent.UpdatePaymentMethodName -> {
                setState {
                    copy(
                        paymentMethodName = event.name,
                        isValidName = event.name.isNotBlank()
                    )
                }
            }
            is PaymentMethodAddEvent.AddPaymentMethod -> {
                requestAddPaymentMethod()
            }
            is PaymentMethodAddEvent.UpdateDefaultPaymentChecked -> {
                setState { copy(isCheckedDefaultPayment = !isCheckedDefaultPayment) }
            }
        }
    }

    private fun requestAddPaymentMethod() {
        viewModelScope.launch {
            upsertPaymentMethodUseCase(
                PaymentMethod(
                    paymentMethodName = uiState.value.paymentMethodName,
                    isDefault = uiState.value.isCheckedDefaultPayment
                )
            ).collect { result ->
                when (result) {
                    ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { PaymentMethodAddSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEffect { PaymentMethodAddSideEffect.NavigateBack }
                    }
                }
            }
        }
    }
}
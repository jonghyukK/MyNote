package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.DeletePaymentMethodUseCase
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui_compose.base.BaseComposeViewModel
import com.kjh.mynote.ui_compose.base.UiEvent
import com.kjh.mynote.ui_compose.base.UiSideEffect
import com.kjh.mynote.ui_compose.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 4..
 * Description:
 */

@HiltViewModel
class PaymentMethodManageViewModel @Inject constructor(
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val deletePaymentMethodUseCase: DeletePaymentMethodUseCase
): BaseComposeViewModel<PaymentMethodManageEvent, PaymentMethodManageUiState, PaymentMethodManageSideEffect>(
    defaultStateBuilder = { PaymentMethodManageUiState() }
) {
    init {
        fetchPaymentMethods()
    }

    override fun reduceState(
        current: PaymentMethodManageUiState,
        event: PaymentMethodManageEvent,
    ): PaymentMethodManageUiState {
        return when (event) {
            is PaymentMethodManageEvent.LoadingPaymentMethods -> {
                current.copy(isLoading = true)
            }
            is PaymentMethodManageEvent.LoadedPaymentMethods -> {
                current.copy(isLoading = false, paymentMethods = event.paymentMethods)
            }
            is PaymentMethodManageEvent.UpdateDeleteDialogVisibility -> {
                current.copy(isVisibleDeleteDialog = event.isVisible, tempDeleteId = event.tempDeleteId)
            }
            else -> current
        }
    }

    override fun handleEvent(event: PaymentMethodManageEvent) {
        when (event) {
            is PaymentMethodManageEvent.DeletePaymentMethod -> {
                deletePaymentMethod(event.paymentMethodId)
            }
            else -> {
                setEvent(event)
            }
        }
    }

    private fun fetchPaymentMethods() {
        viewModelScope.launch {
            observeAllPaymentMethodsUseCase().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PaymentMethodManageEvent.LoadingPaymentMethods)
                    }
                    is ApiResult.Error -> {
                        setEffect(PaymentMethodManageSideEffect.ShowErrorToast(result.error.message))
                        setEvent(PaymentMethodManageEvent.LoadedPaymentMethods(emptyList()))
                    }
                    is ApiResult.Success -> {
                        setEvent(PaymentMethodManageEvent.LoadedPaymentMethods(result.data.toUiModel()))
                    }
                }
            }
        }
    }

    private fun deletePaymentMethod(paymentMethodId: Int) {
        viewModelScope.launch {
            deletePaymentMethodUseCase(paymentMethodId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect(PaymentMethodManageSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        setEvent(
                            PaymentMethodManageEvent.UpdateDeleteDialogVisibility(
                                isVisible = false,
                                tempDeleteId = -1
                            )
                        )
                    }
                }
            }
        }
    }
}


sealed interface PaymentMethodManageSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): PaymentMethodManageSideEffect
}

sealed interface PaymentMethodManageEvent: UiEvent {
    data object LoadingPaymentMethods: PaymentMethodManageEvent
    data class LoadedPaymentMethods(val paymentMethods: List<PaymentMethodUiModel>): PaymentMethodManageEvent
    data class UpdateDeleteDialogVisibility(
        val isVisible: Boolean,
        val tempDeleteId: Int = -1
    ): PaymentMethodManageEvent
    data class DeletePaymentMethod(val paymentMethodId: Int): PaymentMethodManageEvent
}

data class PaymentMethodManageUiState(
    val isLoading: Boolean = false,
    val paymentMethods: List<PaymentMethodUiModel> = emptyList(),
    val isVisibleDeleteDialog: Boolean = false,
    val tempDeleteId: Int = -1
): UiState
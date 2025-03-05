package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.DeletePaymentMethodUseCase
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.example.domain.usecase.UpsertPaymentMethodUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui_compose.base.BaseComposeViewModel
import com.kjh.mynote.ui_compose.base.UiEvent
import com.kjh.mynote.ui_compose.base.UiSideEffect
import com.kjh.mynote.ui_compose.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 4..
 * Description:
 */

sealed interface PaymentMethodManageSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): PaymentMethodManageSideEffect
}

sealed interface PaymentMethodManageEvent: UiEvent {
    data object LoadingPaymentMethods: PaymentMethodManageEvent
    data class LoadedPaymentMethods(val paymentMethods: List<PaymentMethodUiModel>): PaymentMethodManageEvent
    data class UpdateDeleteDialogState(val dialogState: DeleteDialogState): PaymentMethodManageEvent
}

data class PaymentMethodManageUiState(
    val isLoading: Boolean = false,
    val paymentMethods: List<PaymentMethodUiModel> = emptyList(),
    val deleteDialogState: DeleteDialogState = DeleteDialogState()
): UiState

data class DeleteDialogState(
    val show: Boolean = false,
    val paymentMethodId: Int = -1
)

@HiltViewModel
class PaymentMethodManageViewModel @Inject constructor(
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val deletePaymentMethodUseCase: DeletePaymentMethodUseCase
): BaseComposeViewModel<PaymentMethodManageEvent, PaymentMethodManageUiState, PaymentMethodManageSideEffect>(PaymentMethodManageUiState()) {

    override suspend fun reduceState(
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
            is PaymentMethodManageEvent.UpdateDeleteDialogState -> {
                current.copy(deleteDialogState = event.dialogState)
            }
        }
    }

    init {
        fetchPaymentMethods()
    }

    private fun fetchPaymentMethods() {
        viewModelScope.launch {
            observeAllPaymentMethodsUseCase().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PaymentMethodManageEvent.LoadingPaymentMethods)
                    }
                    is ApiResult.Error -> {
                        setEffect { PaymentMethodManageSideEffect.ShowErrorToast(result.error.message) }
                        setEvent(PaymentMethodManageEvent.LoadedPaymentMethods(emptyList()))
                    }
                    is ApiResult.Success -> {
                        setEvent(PaymentMethodManageEvent.LoadedPaymentMethods(result.data.toUiModel()))
                    }
                }
            }
        }
    }

    fun deletePaymentMethod(paymentMethodId: Int) {
        viewModelScope.launch {
            deletePaymentMethodUseCase(paymentMethodId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { PaymentMethodManageSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEvent(
                            PaymentMethodManageEvent.UpdateDeleteDialogState(
                                dialogState = DeleteDialogState(show = false)
                            )
                        )
                    }
                }
            }
        }
    }
}
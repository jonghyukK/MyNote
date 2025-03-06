package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.GetPaymentMethodByIdUseCase
import com.example.domain.usecase.UpsertPaymentMethodUseCase
import com.kjh.mynote.R
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
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

@HiltViewModel
class PaymentMethodAddEditViewModel @Inject constructor(
    private val getPaymentMethodByIdUseCase: GetPaymentMethodByIdUseCase,
    private val upsertPaymentMethodUseCase: UpsertPaymentMethodUseCase,
    private val savedStateHandle: SavedStateHandle,
) : BaseComposeViewModel<PaymentMethodAddEditEvent, PaymentMethodAddEditUiState, PaymentMethodAddEditSideEffect>() {

    private val paymentMethodId: Int = savedStateHandle[KEY_PAYMENT_METHOD_ID] ?: -1

    override fun createInitialState(): PaymentMethodAddEditUiState {
        return PaymentMethodAddEditUiState(
            viewType = if (paymentMethodId == -1) ViewType.Add else ViewType.Edit
        )
    }

    init {
        if (paymentMethodId != -1) {
            fetchPaymentMethod(paymentMethodId)
        }
    }

    private fun fetchPaymentMethod(paymentMethodId: Int) {
        viewModelScope.launch {
            getPaymentMethodByIdUseCase(paymentMethodId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PaymentMethodAddEditEvent.LoadingPaymentMethod)
                    }
                    is ApiResult.Error -> {
                        setEffect { PaymentMethodAddEditSideEffect.ShowErrorToast(result.error.message) }
                        setEvent(PaymentMethodAddEditEvent.LoadedPaymentMethod(null))
                    }
                    is ApiResult.Success -> {
                        val paymentMethodItem = result.data?.toUiModel()
                        setEvent(PaymentMethodAddEditEvent.LoadedPaymentMethod(paymentMethodItem))
                    }
                }
            }
        }
    }

    override suspend fun reduceState(
        current: PaymentMethodAddEditUiState,
        event: PaymentMethodAddEditEvent,
    ): PaymentMethodAddEditUiState {
        return when (event) {
            is PaymentMethodAddEditEvent.LoadingPaymentMethod -> {
                current.copy(isLoading = true)
            }
            is PaymentMethodAddEditEvent.LoadedPaymentMethod -> {
                current.copy(
                    isLoading = false,
                    paymentMethodName = event.paymentMethodItem?.paymentMethodName ?: "",
                    isCheckedDefaultPayment = event.paymentMethodItem?.isDefault ?: false,
                    isValidName = event.paymentMethodItem?.paymentMethodName?.isNotEmpty() ?: false
                )
            }
            is PaymentMethodAddEditEvent.UpdateDefaultPaymentChecked -> {
                current.copy(isCheckedDefaultPayment = !current.isCheckedDefaultPayment)
            }
            is PaymentMethodAddEditEvent.UpdatePaymentMethodName -> {
                current.copy(
                    paymentMethodName = event.name,
                    isValidName = event.name.isNotBlank()
                )
            }
            is PaymentMethodAddEditEvent.AddEditPaymentMethod -> current
        }
    }

    override fun handleEvent(event: PaymentMethodAddEditEvent) {
        when (event) {
            is PaymentMethodAddEditEvent.AddEditPaymentMethod -> {
                if (paymentMethodId == -1) {
                    requestAddPaymentMethod()
                } else {
                    requestEditPaymentMethod()
                }
            }
            else -> setEvent(event)
        }
    }

    private fun requestAddPaymentMethod() {
        viewModelScope.launch {
            upsertPaymentMethodUseCase(
                PaymentMethod(
                    paymentMethodName = state.value.paymentMethodName,
                    isDefault = state.value.isCheckedDefaultPayment
                )
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { PaymentMethodAddEditSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEffect { PaymentMethodAddEditSideEffect.NavigateUp }
                    }
                }
            }
        }
    }

    private fun requestEditPaymentMethod() {
        viewModelScope.launch {
            val paymentMethod = PaymentMethod(
                paymentMethodId = paymentMethodId,
                paymentMethodName = state.value.paymentMethodName,
                isDefault = state.value.isCheckedDefaultPayment
            )

            upsertPaymentMethodUseCase(paymentMethod).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { PaymentMethodAddEditSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEffect { PaymentMethodAddEditSideEffect.NavigateUp }
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_PAYMENT_METHOD_ID = "paymentMethodId"
    }
}

sealed interface PaymentMethodAddEditSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): PaymentMethodAddEditSideEffect
    data object NavigateUp: PaymentMethodAddEditSideEffect
}

sealed interface PaymentMethodAddEditEvent: UiEvent {
    data object LoadingPaymentMethod: PaymentMethodAddEditEvent
    data class LoadedPaymentMethod(val paymentMethodItem: PaymentMethodUiModel?): PaymentMethodAddEditEvent
    data object UpdateDefaultPaymentChecked: PaymentMethodAddEditEvent
    data class UpdatePaymentMethodName(val name: String): PaymentMethodAddEditEvent
    data object AddEditPaymentMethod: PaymentMethodAddEditEvent
}

data class PaymentMethodAddEditUiState(
    val isLoading: Boolean = false,
    val paymentMethodName: String = "",
    val isCheckedDefaultPayment: Boolean = false,
    val isValidName: Boolean = false,
    val viewType: ViewType = ViewType.Add
): UiState

sealed class ViewType {
    abstract val pageTitleRes: Int
    abstract val bottomButtonTextRes: Int

    data object Add: ViewType() {
        override val pageTitleRes: Int
            get() = R.string.make_payment_method
        override val bottomButtonTextRes: Int
            get() = R.string.do_register
    }

    data object Edit: ViewType() {
        override val pageTitleRes: Int
            get() = R.string.edit_payment_method
        override val bottomButtonTextRes: Int
            get() = R.string.do_modify
    }
}
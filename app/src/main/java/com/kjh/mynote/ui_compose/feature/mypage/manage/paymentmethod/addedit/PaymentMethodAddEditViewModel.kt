package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.addedit

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.addedit.navigation.PaymentMethodAddEditRoute
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
): BaseComposeViewModel<PaymentMethodAddEditEvent, PaymentMethodAddEditUiState, PaymentMethodAddEditSideEffect>(
    defaultStateBuilder = {
        val paymentMethodId = savedStateHandle.toRoute<PaymentMethodAddEditRoute>().paymentMethodId
        PaymentMethodAddEditUiState(
            viewType = if (paymentMethodId == -1) ViewType.Add else ViewType.Edit
        )
    }
) {
    val paymentMethodId = savedStateHandle.toRoute<PaymentMethodAddEditRoute>().paymentMethodId

    init {
        if (paymentMethodId != -1) {
            fetchPaymentMethod(paymentMethodId)
        }
    }

    override fun reduceState(
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
                    inputPaymentMethodName = event.paymentMethodItem?.paymentMethodName ?: "",
                    isCheckedDefaultPayment = event.paymentMethodItem?.isDefault ?: false,
                    isValidName = event.paymentMethodItem?.paymentMethodName?.isNotEmpty() ?: false
                )
            }
            is PaymentMethodAddEditEvent.ToggleDefaultPaymentChecked -> {
                current.copy(isCheckedDefaultPayment = !current.isCheckedDefaultPayment)
            }
            is PaymentMethodAddEditEvent.UpdatePaymentMethodName -> {
                current.copy(
                    inputPaymentMethodName = event.name,
                    isValidName = event.name.isNotBlank()
                )
            }
            else -> current
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

    private fun fetchPaymentMethod(paymentMethodId: Int) {
        viewModelScope.launch {
            getPaymentMethodByIdUseCase(paymentMethodId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PaymentMethodAddEditEvent.LoadingPaymentMethod)
                    }
                    is ApiResult.Error -> {
                        setEffect(PaymentMethodAddEditSideEffect.ShowErrorToast(result.error.message))
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

    private fun requestAddPaymentMethod() {
        viewModelScope.launch {
            upsertPaymentMethodUseCase(
                PaymentMethod(
                    paymentMethodName = state.value.inputPaymentMethodName,
                    isDefault = state.value.isCheckedDefaultPayment
                )
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PaymentMethodAddEditEvent.LoadingPaymentMethod)
                    }
                    is ApiResult.Error -> {
                        setEffect(PaymentMethodAddEditSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        setEffect(PaymentMethodAddEditSideEffect.NavigateUp)
                    }
                }
            }
        }
    }

    private fun requestEditPaymentMethod() {
        viewModelScope.launch {
            val paymentMethod = PaymentMethod(
                paymentMethodId = paymentMethodId,
                paymentMethodName = state.value.inputPaymentMethodName,
                isDefault = state.value.isCheckedDefaultPayment
            )

            upsertPaymentMethodUseCase(paymentMethod).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PaymentMethodAddEditEvent.LoadingPaymentMethod)
                    }
                    is ApiResult.Error -> {
                        setEffect(PaymentMethodAddEditSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        setEffect(PaymentMethodAddEditSideEffect.NavigateUp)
                    }
                }
            }
        }
    }
}

sealed interface PaymentMethodAddEditSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): PaymentMethodAddEditSideEffect
    data object NavigateUp: PaymentMethodAddEditSideEffect
}

sealed interface PaymentMethodAddEditEvent: UiEvent {
    data object LoadingPaymentMethod: PaymentMethodAddEditEvent
    data class LoadedPaymentMethod(val paymentMethodItem: PaymentMethodUiModel?): PaymentMethodAddEditEvent
    data object ToggleDefaultPaymentChecked: PaymentMethodAddEditEvent
    data class UpdatePaymentMethodName(val name: String): PaymentMethodAddEditEvent
    data object AddEditPaymentMethod: PaymentMethodAddEditEvent
}

data class PaymentMethodAddEditUiState(
    val isLoading: Boolean = false,
    val inputPaymentMethodName: String = "",
    val isCheckedDefaultPayment: Boolean = false,
    val isValidName: Boolean = false,
    val viewType: ViewType = ViewType.Add
): UiState

sealed class ViewType {
    abstract val pageTitleRes: Int
    abstract val bottomButtonTextRes: Int

    data object Add: ViewType() {
        override val pageTitleRes: Int = R.string.make_payment_method
        override val bottomButtonTextRes: Int = R.string.do_register
    }

    data object Edit: ViewType() {
        override val pageTitleRes: Int = R.string.edit_payment_method
        override val bottomButtonTextRes: Int = R.string.do_modify
    }
}
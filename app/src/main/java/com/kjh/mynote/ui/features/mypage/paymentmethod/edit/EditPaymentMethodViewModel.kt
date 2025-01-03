package com.kjh.mynote.ui.features.mypage.paymentmethod.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.UpdatePaymentMethodUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.ui.features.mypage.paymentmethod.edit.EditPaymentMethodDialogFragment.Companion.ARG_PAYMENT_METHOD_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@HiltViewModel
class EditPaymentMethodViewModel @Inject constructor(
    private val updatePaymentMethodUseCase: UpdatePaymentMethodUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _initPaymentMethodItem =
        savedStateHandle.getStateFlow(ARG_PAYMENT_METHOD_ITEM, PaymentMethodUiModel())

    private val _paymentMethodNameText = MutableStateFlow(_initPaymentMethodItem.value.paymentMethodName)
    val paymentMethodNameText = _paymentMethodNameText.asStateFlow()

    private val _editPaymentMethodEvent = MutableSharedFlow<EditPaymentMethodEventState>()
    val editPaymentMethodEvent = _editPaymentMethodEvent.asSharedFlow()

    val isValidData = combine(
        _initPaymentMethodItem, _paymentMethodNameText
    ) { initItem, inputText ->
        inputText.isNotBlank() && inputText != initItem.paymentMethodName
    }

    fun editPaymentMethod() {
        viewModelScope.launch {
            val targetPaymentMethodItem = _initPaymentMethodItem.value.copy(
                paymentMethodName = _paymentMethodNameText.value
            )

            updatePaymentMethodUseCase(
                paymentMethod = targetPaymentMethodItem.toDomainModal()
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _editPaymentMethodEvent.emit(EditPaymentMethodEventState.Loading)
                    is ApiResult.Error ->
                        _editPaymentMethodEvent.emit(EditPaymentMethodEventState.Error(result.error))
                    is ApiResult.Success -> {
                        _editPaymentMethodEvent.emit(EditPaymentMethodEventState.Success(targetPaymentMethodItem))
                    }
                }
            }
        }
    }

    fun setPaymentMethodName(name: String) {
        _paymentMethodNameText.value = name
    }
}

sealed interface EditPaymentMethodEventState {
    data object Loading: EditPaymentMethodEventState
    data class Error(val error: Throwable): EditPaymentMethodEventState
    data class Success(val paymentMethodItem: PaymentMethodUiModel): EditPaymentMethodEventState
}
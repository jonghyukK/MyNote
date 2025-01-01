package com.kjh.mynote.ui.features.mypage.paymentmethod.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.MakePaymentMethodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@HiltViewModel
class MakePaymentMethodViewModel @Inject constructor(
    private val makePaymentMethodUseCase: MakePaymentMethodUseCase
): ViewModel() {

    private val _makePaymentMethodEvent = MutableSharedFlow<MakePaymentMethodEventState>()
    val makePaymentMethodEvent = _makePaymentMethodEvent.asSharedFlow()

    private val _paymentMethodNameText = MutableStateFlow("")
    val paymentMethodNameText = _paymentMethodNameText.asStateFlow()

    fun makePaymentMethod() {
        viewModelScope.launch {
            val paymentMethod = PaymentMethod(paymentMethodName = _paymentMethodNameText.value)

            makePaymentMethodUseCase(paymentMethod).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _makePaymentMethodEvent.emit(MakePaymentMethodEventState.Loading)
                    is ApiResult.Error ->
                        _makePaymentMethodEvent.emit(MakePaymentMethodEventState.Error(result.error))
                    is ApiResult.Success -> {
                        _makePaymentMethodEvent.emit(MakePaymentMethodEventState.Success(result.data.toInt()))
                    }
                }
            }
        }
    }

    fun setPaymentMethodName(name: String) {
        _paymentMethodNameText.value = name
    }
}

sealed interface MakePaymentMethodEventState {
    data object Loading: MakePaymentMethodEventState
    data class Error(val error: Throwable): MakePaymentMethodEventState
    data class Success(val madePaymentMethodId: Int): MakePaymentMethodEventState
}
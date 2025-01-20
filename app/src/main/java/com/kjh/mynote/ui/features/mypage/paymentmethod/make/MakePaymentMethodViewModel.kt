package com.kjh.mynote.ui.features.mypage.paymentmethod.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.UpsertPaymentMethodUseCase
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
 * Created On 2024. 12. 31..
 * Description:
 */

@HiltViewModel
class MakePaymentMethodViewModel @Inject constructor(
    private val upsertPaymentMethodUseCase: UpsertPaymentMethodUseCase
): ViewModel() {

    private val _makePaymentMethodEvent = MutableSharedFlow<MakePaymentMethodEventState>()
    val makePaymentMethodEvent = _makePaymentMethodEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(PaymentMethod())
    val uiState = _uiState.asStateFlow()

    fun makePaymentMethod() {
        viewModelScope.launch {
            upsertPaymentMethodUseCase(_uiState.value).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _makePaymentMethodEvent.emit(MakePaymentMethodEventState.Loading)

                    is ApiResult.Error ->
                        _makePaymentMethodEvent.emit(MakePaymentMethodEventState.Error(
                            result.error.message ?: "결제수단 등록이 실패하였습니다."))

                    is ApiResult.Success -> {
                        _makePaymentMethodEvent.emit(MakePaymentMethodEventState.Success(result.data.toInt()))
                    }
                }
            }
        }
    }

    fun setPaymentMethodName(name: String) {
        _uiState.update { uiState ->
            uiState.copy(paymentMethodName = name)
        }
    }

    fun toggleDefaultState() {
        _uiState.update { uiState ->
            uiState.copy(isDefault = !uiState.isDefault)
        }
    }
}

sealed interface MakePaymentMethodEventState {
    data object Loading: MakePaymentMethodEventState
    data class Error(val errorMsg: String): MakePaymentMethodEventState
    data class Success(val madePaymentMethodId: Int): MakePaymentMethodEventState
}
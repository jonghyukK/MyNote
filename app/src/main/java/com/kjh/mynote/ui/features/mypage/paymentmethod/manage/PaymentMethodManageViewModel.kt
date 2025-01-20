package com.kjh.mynote.ui.features.mypage.paymentmethod.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.DeletePaymentMethodUseCase
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@HiltViewModel
class PaymentMethodManageViewModel @Inject constructor(
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    private val deletePaymentMethodUseCase: DeletePaymentMethodUseCase
): ViewModel() {

    private val _shownFetchError = MutableStateFlow(false)

    private val _deletePaymentMethodEvent = MutableSharedFlow<DeletePaymentMethodEventState>()
    val deletePaymentMethodEvent = _deletePaymentMethodEvent.asSharedFlow()

    val uiState: StateFlow<PaymentMethodManageUiState> = combine(
        _shownFetchError, getPaymentMethodsUseCase()
    ) { shownFetchError, result ->
        when (result) {
            is ApiResult.Loading ->
                PaymentMethodManageUiState.Loading

            is ApiResult.Error ->
                PaymentMethodManageUiState.Error(
                    if (shownFetchError) null else result.error.message ?: "결제수단 목록 조회가 실패하였습니다.")

            is ApiResult.Success ->
                PaymentMethodManageUiState.PaymentMethods(result.data.toUiModel())
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaymentMethodManageUiState.Loading
        )

    fun deletePaymentMethod(id: Int) {
        viewModelScope.launch {
            deletePaymentMethodUseCase(id).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _deletePaymentMethodEvent.emit(DeletePaymentMethodEventState.Loading)

                    is ApiResult.Error ->
                        _deletePaymentMethodEvent.emit(DeletePaymentMethodEventState.Error(
                            result.error.message ?: "결제수단 삭제가 실패하였습니다."))

                    is ApiResult.Success ->
                        _deletePaymentMethodEvent.emit(DeletePaymentMethodEventState.Success)
                }
            }
        }
    }

    fun shownFetchError() {
        _shownFetchError.value = true
    }
}

sealed interface DeletePaymentMethodEventState {
    data object Loading: DeletePaymentMethodEventState
    data class Error(val errorMsg: String): DeletePaymentMethodEventState
    data object Success: DeletePaymentMethodEventState
}

sealed interface PaymentMethodManageUiState {
    data object Loading: PaymentMethodManageUiState
    data class Error(val errorMsg: String?): PaymentMethodManageUiState
    data class PaymentMethods(val items: List<PaymentMethodUiModel>): PaymentMethodManageUiState
}
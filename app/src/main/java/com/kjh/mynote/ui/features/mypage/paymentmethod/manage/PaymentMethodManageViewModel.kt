package com.kjh.mynote.ui.features.mypage.paymentmethod.manage

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.DeletePaymentMethodUseCase
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val deletePaymentMethodUseCase: DeletePaymentMethodUseCase
): BaseViewModel() {

    private val _deletePaymentMethodEvent = MutableSharedFlow<DeletePaymentMethodEventState>()
    val deletePaymentMethodEvent = _deletePaymentMethodEvent.asSharedFlow()

    val uiState: StateFlow<UiState<List<PaymentMethodUiModel>>> =
        observeAllPaymentMethodsUseCase()
            .mapResultToUiState { it.toUiModel() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState.Loading
            )

    fun deletePaymentMethod(id: Int) {
        viewModelScope.launch {
            deletePaymentMethodUseCase(id).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _deletePaymentMethodEvent.emit(DeletePaymentMethodEventState.Loading)

                    is ApiResult.Error -> {
                        sendError(result.error.message)
                        _deletePaymentMethodEvent.emit(DeletePaymentMethodEventState.Error)
                    }

                    is ApiResult.Success ->
                        _deletePaymentMethodEvent.emit(DeletePaymentMethodEventState.Success)
                }
            }
        }
    }
}

sealed interface DeletePaymentMethodEventState {
    data object Loading: DeletePaymentMethodEventState
    data object Error: DeletePaymentMethodEventState
    data object Success: DeletePaymentMethodEventState
}
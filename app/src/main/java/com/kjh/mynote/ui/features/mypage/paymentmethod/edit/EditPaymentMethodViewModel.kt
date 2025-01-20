package com.kjh.mynote.ui.features.mypage.paymentmethod.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.UpsertPaymentMethodUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.ui.features.mypage.paymentmethod.edit.EditPaymentMethodDialogFragment.Companion.ARG_PAYMENT_METHOD_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@HiltViewModel
class EditPaymentMethodViewModel @Inject constructor(
    private val upsertPaymentMethodUseCase: UpsertPaymentMethodUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val initPaymentMethodItem =
        savedStateHandle.getStateFlow(ARG_PAYMENT_METHOD_ITEM, PaymentMethodUiModel())

    private val _uiState = MutableStateFlow(initPaymentMethodItem.value)
    val uiState = _uiState.asStateFlow()

    private val _editPaymentMethodEvent = MutableSharedFlow<EditPaymentMethodEventState>()
    val editPaymentMethodEvent = _editPaymentMethodEvent.asSharedFlow()

    val editButtonEnable: Flow<Boolean> = combine(
        initPaymentMethodItem, _uiState
    ) { initItem, uiState ->
        uiState.paymentMethodName.isNotBlank()
                && (uiState.paymentMethodName != initItem.paymentMethodName
                || uiState.isDefault != initItem.isDefault)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    fun editPaymentMethod() {
        viewModelScope.launch {
            upsertPaymentMethodUseCase(
                paymentMethod = _uiState.value.toDomainModal()
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _editPaymentMethodEvent.emit(EditPaymentMethodEventState.Loading)

                    is ApiResult.Error ->
                        _editPaymentMethodEvent.emit(EditPaymentMethodEventState.Error(
                            result.error.message ?: "결제수단 수정이 실패하였습니다."))

                    is ApiResult.Success -> {
                        _editPaymentMethodEvent.emit(EditPaymentMethodEventState.Success)
                    }
                }
            }
        }
    }

    fun setPaymentMethodName(name: String) {
        _uiState.update {
            it.copy(paymentMethodName = name)
        }
    }

    fun toggleDefaultState() {
        _uiState.update {
            it.copy(isDefault = !it.isDefault)
        }
    }
}

sealed interface EditPaymentMethodEventState {
    data object Loading: EditPaymentMethodEventState
    data class Error(val errorMsg: String): EditPaymentMethodEventState
    data object Success: EditPaymentMethodEventState
}
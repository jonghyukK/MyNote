package com.kjh.mynote.ui.features.mypage.paymentmethod.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.asResult
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

sealed interface PaymentMethodManageUiState {
    data object Loading: PaymentMethodManageUiState
    data class Error(val error: Throwable): PaymentMethodManageUiState
    data class PaymentMethods(val items: List<PaymentMethodUiModel>): PaymentMethodManageUiState
}

@HiltViewModel
class PaymentMethodManageViewModel @Inject constructor(
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase
): ViewModel() {

    val uiState = getPaymentMethodsUseCase()
        .asResult()
        .map { result ->
            when (result) {
                is ApiResult.Loading ->
                    PaymentMethodManageUiState.Loading

                is ApiResult.Error ->
                    PaymentMethodManageUiState.Error(result.error)

                is ApiResult.Success ->
                    PaymentMethodManageUiState.PaymentMethods(result.data.toUiModel())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaymentMethodManageUiState.Loading
        )


}
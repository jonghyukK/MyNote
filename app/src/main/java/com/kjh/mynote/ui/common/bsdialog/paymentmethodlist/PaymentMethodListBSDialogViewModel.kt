package com.kjh.mynote.ui.common.bsdialog.paymentmethodlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.ui.common.bsdialog.paymentmethodlist.PaymentMethodListBSDialog.Companion.ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */

@HiltViewModel
class PaymentMethodListBSDialogViewModel @Inject constructor(
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val selectedPaymentMethodItem: PaymentMethodUiModel?
        get() = savedStateHandle[ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM]

    private val _updateSelectedItemEvent = Channel<PaymentMethodUiModel?>()
    val updateSelectedItemEvent = _updateSelectedItemEvent.receiveAsFlow()

    val uiState: StateFlow<UiState<List<SelectablePaymentMethodItem>>> =
        observeAllPaymentMethodsUseCase()
            .onEach {
                if (it is ApiResult.Success) {
                    updateSelectedPaymentMethodItem(it.data.toUiModel())
                }
            }
            .mapResultToUiState { paymentMethods ->
                paymentMethods.map {
                    SelectablePaymentMethodItem(
                        isSelected = it.paymentMethodId == selectedPaymentMethodItem?.paymentMethodId,
                        paymentMethodItem = it.toUiModel()
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState.Loading
            )

    private suspend fun updateSelectedPaymentMethodItem(paymentMethodItems: List<PaymentMethodUiModel>) {
        val matchedIdItem = paymentMethodItems.find {
            it.paymentMethodId == selectedPaymentMethodItem?.paymentMethodId
        }

        if (matchedIdItem != selectedPaymentMethodItem) {
            _updateSelectedItemEvent.send(matchedIdItem)
            savedStateHandle[ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM] = matchedIdItem
        }
    }
}

data class SelectablePaymentMethodItem(
    val isSelected: Boolean = false,
    val paymentMethodItem: PaymentMethodUiModel
)
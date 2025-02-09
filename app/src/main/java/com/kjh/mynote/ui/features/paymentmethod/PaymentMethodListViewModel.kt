package com.kjh.mynote.ui.features.paymentmethod

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.ui.features.paymentmethod.PaymentMethodListBSDialog.Companion.ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 3..
 * Description:
 */

@HiltViewModel
class PaymentMethodListViewModel @Inject constructor(
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val selectedPaymentMethodItem = MutableStateFlow(
        savedStateHandle.get<PaymentMethodUiModel?>(ARG_OBJ_SELECTED_PAYMENT_METHOD_ITEM))

    private val _updateSelectedItemEvent = Channel<PaymentMethodUiModel?>()
    val updateSelectedItemEvent = _updateSelectedItemEvent.receiveAsFlow()

    val paymentMethodItems: StateFlow<List<SelectablePaymentMethodItem>> =
        observeAllPaymentMethodsUseCase()
            .onEach {
                if (it is ApiResult.Success) {
                    updateSelectedPaymentMethodItem(it.data.toUiModel())
                }
            }
            .map(::mapResultToList)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private fun mapResultToList(
        result: ApiResult<List<PaymentMethod>>
    ): List<SelectablePaymentMethodItem> {
        return when (result) {
            is ApiResult.Loading -> emptyList()

            is ApiResult.Error -> {
                sendError(result.error.message)
                emptyList()
            }

            is ApiResult.Success -> {
                result.data.toUiModel().map { paymentMethod ->
                    SelectablePaymentMethodItem(
                        isSelected = paymentMethod.paymentMethodId == selectedPaymentMethodItem.value?.paymentMethodId,
                        paymentMethodItem = paymentMethod
                    )
                }
            }
        }
    }

    private suspend fun updateSelectedPaymentMethodItem(paymentMethodItems: List<PaymentMethodUiModel>) {
        val matchedIdItem = paymentMethodItems.find {
            it.paymentMethodId == selectedPaymentMethodItem.value?.paymentMethodId
        }

        if (matchedIdItem != selectedPaymentMethodItem.value) {
            _updateSelectedItemEvent.send(matchedIdItem)
            selectedPaymentMethodItem.value = matchedIdItem
        }
    }
}

data class SelectablePaymentMethodItem(
    val isSelected: Boolean = false,
    val paymentMethodItem: PaymentMethodUiModel
)
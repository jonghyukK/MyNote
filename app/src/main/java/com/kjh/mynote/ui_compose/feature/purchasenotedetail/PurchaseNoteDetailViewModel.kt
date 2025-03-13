package com.kjh.mynote.ui_compose.feature.purchasenotedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceInfo
import com.example.domain.usecase.DeletePurchaseNoteByIdUseCase
import com.example.domain.usecase.ObservePurchaseNoteByIdUseCase
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui_compose.base.BaseComposeViewModel
import com.kjh.mynote.ui_compose.base.UiEvent
import com.kjh.mynote.ui_compose.base.UiSideEffect
import com.kjh.mynote.ui_compose.base.UiState
import com.kjh.mynote.ui_compose.feature.purchasenotedetail.navigation.PurchaseNoteDetailRoute
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 13..
 * Description:
 */

sealed class PurchaseNoteDetailSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): PurchaseNoteDetailSideEffect()
    data object NavigateUp: PurchaseNoteDetailSideEffect()
}

sealed class PurchaseNoteDetailUiEvent: UiEvent {
    data class UpdateLoading(val isLoading: Boolean): PurchaseNoteDetailUiEvent()
    data class LoadedDetailItem(val purchaseNoteItem: PurchaseNoteUiModel?): PurchaseNoteDetailUiEvent()
    data class UpdateDeleteDialogVisibility(val isVisible: Boolean): PurchaseNoteDetailUiEvent()
    data object DeletePurchaseNote: PurchaseNoteDetailUiEvent()
}

data class PurchaseNoteDetailUiState(
    val isLoading: Boolean = false,
    val isVisibleDeleteDialog: Boolean = false,
    val purchaseNoteItem: PurchaseNoteUiModel = PurchaseNoteUiModel.default
): UiState

@HiltViewModel
class PurchaseNoteDetailViewModel @Inject constructor(
    private val observePurchaseNoteByIdUseCase: ObservePurchaseNoteByIdUseCase,
    private val deletePurchaseNoteByIdUseCase: DeletePurchaseNoteByIdUseCase,
    private val savedStateHandle: SavedStateHandle,
) : BaseComposeViewModel<PurchaseNoteDetailUiEvent, PurchaseNoteDetailUiState, PurchaseNoteDetailSideEffect>(
    { PurchaseNoteDetailUiState() }) {

    private val purchaseNoteId = savedStateHandle.toRoute<PurchaseNoteDetailRoute>().purchaseNoteId

    init {
        fetchPurchaseNoteDetail(purchaseNoteId)
    }

    override fun handleEvent(event: PurchaseNoteDetailUiEvent) {
        when (event) {
            is PurchaseNoteDetailUiEvent.DeletePurchaseNote -> {
                requestDeletePurchaseNote()
            }
            else -> setEvent(event)
        }
    }

    override fun reduceState(
        current: PurchaseNoteDetailUiState,
        event: PurchaseNoteDetailUiEvent,
    ): PurchaseNoteDetailUiState {
        return when (event) {
            is PurchaseNoteDetailUiEvent.UpdateLoading -> {
                current.copy(isLoading = event.isLoading)
            }
            is PurchaseNoteDetailUiEvent.LoadedDetailItem -> {
                val purchaseNoteItem = event.purchaseNoteItem ?: return current
                current.copy(
                    isLoading = false,
                    purchaseNoteItem = purchaseNoteItem
                )
            }
            is PurchaseNoteDetailUiEvent.UpdateDeleteDialogVisibility -> {
                current.copy(isVisibleDeleteDialog = event.isVisible)
            }
            else -> current
        }
    }

    private fun fetchPurchaseNoteDetail(purchaseNoteId: Int) {
        viewModelScope.launch {
            observePurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PurchaseNoteDetailUiEvent.UpdateLoading(true))
                    }
                    is ApiResult.Error -> {
                        setEffect(PurchaseNoteDetailSideEffect.ShowErrorToast(result.error.message))
                        setEvent(PurchaseNoteDetailUiEvent.LoadedDetailItem(null))
                    }
                    is ApiResult.Success -> {
                        val purchaseNoteItem = result.data?.toUiModel()
                        setEvent(PurchaseNoteDetailUiEvent.LoadedDetailItem(purchaseNoteItem))
                    }
                }
            }
        }
    }

    private fun requestDeletePurchaseNote() {
        viewModelScope.launch {
            deletePurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        setEvent(PurchaseNoteDetailUiEvent.UpdateLoading(true))
                    }
                    is ApiResult.Error -> {
                        setEffect(PurchaseNoteDetailSideEffect.ShowErrorToast(result.error.message))
                        setEvent(PurchaseNoteDetailUiEvent.UpdateLoading(false))
                    }
                    is ApiResult.Success -> {
                        setEvent(PurchaseNoteDetailUiEvent.UpdateLoading(false))
                        setEffect(PurchaseNoteDetailSideEffect.NavigateUp)
                    }
                }
            }
        }
    }
}
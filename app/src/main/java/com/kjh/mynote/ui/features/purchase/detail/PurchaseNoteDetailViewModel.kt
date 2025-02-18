package com.kjh.mynote.ui.features.purchase.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.DeletePurchaseNoteByIdUseCase
import com.example.domain.usecase.ObservePurchaseNoteByIdUseCase
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
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
 * Created On 2024. 11. 12..
 * Description:
 */

@HiltViewModel
class PurchaseNoteDetailViewModel @Inject constructor(
    private val observePurchaseNoteByIdUseCase: ObservePurchaseNoteByIdUseCase,
    private val deletePurchaseNoteByIdUseCase: DeletePurchaseNoteByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    val purchaseNoteId: Int
        get() = savedStateHandle[AppConstants.INTENT_PURCHASE_NOTE_ID] ?: -1

    private val _deleteEventState = MutableSharedFlow<DeletePurchaseNoteEventState>()
    val deleteEventState = _deleteEventState.asSharedFlow()

    val uiState: StateFlow<PurchaseNoteDetailUiState> =
        observePurchaseNoteByIdUseCase(purchaseNoteId)
            .mapResultToState(
                onLoading = { PurchaseNoteDetailUiState.Loading },
                onError = { PurchaseNoteDetailUiState.Error },
                onSuccess = { purchaseNote ->
                    if (purchaseNote == null) {
                        PurchaseNoteDetailUiState.NotExist
                    } else {
                        PurchaseNoteDetailUiState.Success(purchaseNote.toUiModel())
                    }
                }
            )
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                PurchaseNoteDetailUiState.Loading
            )

    fun deletePurchaseNote() {
        viewModelScope.launch {
            deletePurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _deleteEventState.emit(DeletePurchaseNoteEventState.Loading)

                    is ApiResult.Error -> {
                        sendError(result.error.message)
                        _deleteEventState.emit(DeletePurchaseNoteEventState.Error)
                    }

                    is ApiResult.Success ->
                        _deleteEventState.emit(DeletePurchaseNoteEventState.Success)
                }
            }
        }
    }
}

sealed interface DeletePurchaseNoteEventState {
    data object Loading: DeletePurchaseNoteEventState
    data object Error: DeletePurchaseNoteEventState
    data object Success: DeletePurchaseNoteEventState
}

sealed interface PurchaseNoteDetailUiState {
    data object Loading: PurchaseNoteDetailUiState
    data object NotExist: PurchaseNoteDetailUiState
    data object Error: PurchaseNoteDetailUiState
    data class Success(val purchaseNoteItem: PurchaseNoteUiModel): PurchaseNoteDetailUiState
}
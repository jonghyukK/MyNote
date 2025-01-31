package com.kjh.mynote.ui.features.purchase.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.asResult
import com.example.domain.usecase.DeletePurchaseNoteByIdUseCase
import com.example.domain.usecase.ObservePurchaseNoteByIdUseCase
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
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
): ViewModel() {

    private val purchaseNoteId: Int = savedStateHandle[AppConstants.INTENT_PURCHASE_NOTE_ID] ?: -1

    private var isDeleted: Boolean = false

    private val _deleteEventState = MutableSharedFlow<DeletePurchaseNoteEvent>()
    val deleteEventState = _deleteEventState.asSharedFlow()

    val uiState: StateFlow<PurchaseNoteDetailUiState> =
        observePurchaseNoteByIdUseCase(purchaseNoteId)
            .asResult()
            .map { result ->
                if (isDeleted) return@map PurchaseNoteDetailUiState.Loading
                when (result) {
                    is ApiResult.Loading -> {
                        PurchaseNoteDetailUiState.Loading
                    }

                    is ApiResult.Error -> {
                        val errorMsg = result.error.message ?: "구매노트 상세 조회가 실패하였습니다."
                        PurchaseNoteDetailUiState.Error(errorMsg)
                    }

                    is ApiResult.Success -> {
                        PurchaseNoteDetailUiState.PurchaseNoteDetail(result.data.toUiModel())
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PurchaseNoteDetailUiState.Loading
            )

    fun deletePurchaseNote() {
        viewModelScope.launch {
            deletePurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _deleteEventState.emit(DeletePurchaseNoteEvent.Loading)
                    }
                    is ApiResult.Error -> {
                        val errorMsg = result.error.message ?: "구매노트 삭제가 실패하였습니다."
                        _deleteEventState.emit(DeletePurchaseNoteEvent.Error(errorMsg))
                    }
                    is ApiResult.Success -> {
                        isDeleted = true
                        _deleteEventState.emit(DeletePurchaseNoteEvent.Success)
                    }
                }
            }
        }
    }
}

sealed interface DeletePurchaseNoteEvent {
    data object Loading: DeletePurchaseNoteEvent
    data class Error(val errorMsg: String): DeletePurchaseNoteEvent
    data object Success: DeletePurchaseNoteEvent
}

sealed interface PurchaseNoteDetailUiState {
    data object Loading: PurchaseNoteDetailUiState
    data class Error(val errorMsg: String): PurchaseNoteDetailUiState
    data class PurchaseNoteDetail(val item: PurchaseNoteUiModel): PurchaseNoteDetailUiState
}
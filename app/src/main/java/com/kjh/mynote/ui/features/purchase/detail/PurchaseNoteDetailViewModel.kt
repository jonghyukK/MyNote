package com.kjh.mynote.ui.features.purchase.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.usecase.DeletePurchaseNoteByIdUseCase
import com.example.domain.usecase.GetPurchaseNoteByIdUseCase
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
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
 * Created On 2024. 11. 12..
 * Description:
 */

data class PurchaseNoteDetailUiState(
    val purchaseNoteItem: PurchaseNoteUiModel? = null,
)

@HiltViewModel
class PurchaseNoteDetailViewModel @Inject constructor(
    private val getPurchaseNoteByIdUseCase: GetPurchaseNoteByIdUseCase,
    private val deletePurchaseNoteByIdUseCase: DeletePurchaseNoteByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val purchaseNoteId: Int = savedStateHandle[AppConstants.INTENT_PURCHASE_NOTE_ID] ?: -1

    private val _uiState = MutableStateFlow(PurchaseNoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _deleteEventState = MutableSharedFlow<UiState<Unit>>()
    val deleteEventState = _deleteEventState.asSharedFlow()

    fun getPurchaseNoteById() {
        viewModelScope.launch {
            getPurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Error -> {}
                    is Result.Success -> {
                        result.data?.let { data ->
                            val purchaseNoteItem = data.toUiModel()

                            _uiState.update {
                                it.copy(
                                    purchaseNoteItem = purchaseNoteItem
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun deletePurchaseNote() {
        viewModelScope.launch {
            deletePurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _deleteEventState.emit(UiState.Loading)
                    }
                    is Result.Error -> {
                        _deleteEventState.emit(UiState.Error(result.msg ?: "구매노트 삭제가 실패하였습니다."))
                    }
                    is Result.Success -> {
                        _deleteEventState.emit(UiState.Success(Unit))
                    }
                }
            }
        }
    }
}
package com.kjh.mynote.ui.features.purchase.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.DeletePurchaseNoteByIdUseCase
import com.example.domain.usecase.GetPurchaseNoteByIdUseCase
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */

@HiltViewModel
class PurchaseNoteDetailViewModel @Inject constructor(
    private val getPurchaseNoteByIdUseCase: GetPurchaseNoteByIdUseCase,
    private val deletePurchaseNoteByIdUseCase: DeletePurchaseNoteByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val purchaseNoteId: Int = savedStateHandle[AppConstants.INTENT_PURCHASE_NOTE_ID] ?: -1

    private val _uiState = MutableStateFlow(PurchaseNoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchPurchaseNoteDetail() {
        viewModelScope.launch {
            getPurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMsg = result.error.message) }
                    }
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, purchaseNoteItem = result.data.toUiModel()) }
                    }
                }
            }
        }
    }

    fun deletePurchaseNote() {
        viewModelScope.launch {
            deletePurchaseNoteByIdUseCase(purchaseNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMsg = result.error.message) }
                    }
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, isSuccessDelete = true) }
                    }
                }
            }
        }
    }

    fun shownError() {
        _uiState.update { it.copy(errorMsg = null) }
    }
}

data class PurchaseNoteDetailUiState(
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val purchaseNoteItem: PurchaseNoteUiModel? = null,
    val isSuccessDelete: Boolean = false
)
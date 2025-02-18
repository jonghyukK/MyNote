package com.kjh.mynote.ui.features.place.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.DeletePlaceNoteByIdUseCase
import com.example.domain.usecase.ObservePlaceNoteByIdUseCase
import com.example.domain.usecase.ObservePlaceNotesByPlaceNameUseCase
import com.example.domain.usecase.ObservePurchaseNotesByPlaceAndDateUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 19..
 * Description:
 */

@HiltViewModel
class PlaceNoteDetailViewModel @Inject constructor(
    private val observePlaceNoteByIdUseCase: ObservePlaceNoteByIdUseCase,
    private val observePlaceNotesByPlaceNameUseCase: ObservePlaceNotesByPlaceNameUseCase,
    private val observePurchaseNotesByPlaceAndDateUseCase: ObservePurchaseNotesByPlaceAndDateUseCase,
    private val deletePlaceNoteByIdUseCase: DeletePlaceNoteByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    val noteId = savedStateHandle[AppConstants.INTENT_NOTE_ID] ?: -1

    private val _deleteEventState = MutableSharedFlow<DeletePlaceNoteEventState>()
    val deleteEventState = _deleteEventState.asSharedFlow()

    val uiState: StateFlow<PlaceNoteDetailUiState> = observePlaceNoteByIdUseCase(noteId)
        .flatMapLatest { placeNoteResult ->
            when (placeNoteResult) {
                is ApiResult.Loading -> flowOf(PlaceNoteDetailUiState.Loading)
                is ApiResult.Error -> flowOf(PlaceNoteDetailUiState.Error)
                is ApiResult.Success -> {
                    val placeNote = placeNoteResult.data?.toUiModel()
                    if (placeNote == null) {
                        flowOf(PlaceNoteDetailUiState.NotExist)
                    } else {
                        val placeName = placeNote.placeInfo.placeName
                        val visitDate = placeNote.visitDate

                        combineApiResults(
                            observePlaceNotesByPlaceNameUseCase(placeName),
                            observePurchaseNotesByPlaceAndDateUseCase(placeName, visitDate),
                            onLoading = { PlaceNoteDetailUiState.Loading },
                            onError = { PlaceNoteDetailUiState.Error }
                        ) { samePlaceNotes, purchaseNotes ->
                            val filteredSamePlaceNotes = samePlaceNotes
                                .filter { it.id != noteId }
                                .map { it.toUiModel() }

                            val uiItems = makeUiItems(
                                placeNote = placeNote,
                                samePlaceNameNotes = filteredSamePlaceNotes,
                                purchaseNotes = purchaseNotes.toUiModel()
                            )

                            PlaceNoteDetailUiState.Success(uiItems)
                        }
                    }
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PlaceNoteDetailUiState.Loading
        )

    fun deletePlaceNote() {
        viewModelScope.launch {
            deletePlaceNoteByIdUseCase(noteId).collect { result ->
                when (result) {
                    is ApiResult.Loading ->
                        _deleteEventState.emit(DeletePlaceNoteEventState.Loading)

                    is ApiResult.Error -> {
                        sendError(result.error.message)
                        _deleteEventState.emit(DeletePlaceNoteEventState.Error)
                    }

                    is ApiResult.Success ->
                        _deleteEventState.emit(DeletePlaceNoteEventState.Success)
                }
            }
        }
    }

    private fun makeUiItems(
        placeNote: PlaceNoteUiModel,
        samePlaceNameNotes: List<PlaceNoteUiModel>,
        purchaseNotes: List<PurchaseNoteUiModel>
    ): List<PlaceNoteDetailUiItemState> {
        val uiItems: MutableList<PlaceNoteDetailUiItemState> = mutableListOf()

        uiItems.add(PlaceNoteDetailUiItemState.PlaceDetailInfoItem(placeNote))

        if (purchaseNotes.isNotEmpty()) {
            uiItems.add(PlaceNoteDetailUiItemState.PurchaseNotesItem(purchaseNoteItems = purchaseNotes))
        }

        if (samePlaceNameNotes.isNotEmpty()) {
            uiItems.add(PlaceNoteDetailUiItemState.SamePlaceNameNotesItem(placeNoteItems = samePlaceNameNotes))
        }

        return uiItems
    }
}

sealed interface DeletePlaceNoteEventState {
    data object Loading: DeletePlaceNoteEventState
    data object Error: DeletePlaceNoteEventState
    data object Success: DeletePlaceNoteEventState
}

sealed interface PlaceNoteDetailUiItemState {
    data class PlaceDetailInfoItem(
        val placeNoteItem: PlaceNoteUiModel
    ): PlaceNoteDetailUiItemState

    data class SamePlaceNameNotesItem(
        val placeNoteItems: List<PlaceNoteUiModel>
    ): PlaceNoteDetailUiItemState

    data class PurchaseNotesItem(
        val purchaseNoteItems: List<PurchaseNoteUiModel>
    ): PlaceNoteDetailUiItemState
}

sealed interface PlaceNoteDetailUiState {
    data object Loading: PlaceNoteDetailUiState
    data object NotExist: PlaceNoteDetailUiState
    data object Error: PlaceNoteDetailUiState
    data class Success(val uiItems: List<PlaceNoteDetailUiItemState> = emptyList()): PlaceNoteDetailUiState
}
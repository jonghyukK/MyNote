package com.kjh.mynote.ui.features.place.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.model.onError
import com.example.domain.model.onLoading
import com.example.domain.model.onSuccess
import com.example.domain.usecase.DeletePlaceNoteByIdUseCase
import com.example.domain.usecase.GetPlaceNoteDetailByIdUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 19..
 * Description:
 */

sealed class PlaceNoteDetailUi {
    data class DetailItem(
        val placeNoteItem: PlaceNoteUiModel
    ): PlaceNoteDetailUi()

    data class SectionTitleItem(
        val sectionTitle: String
    ): PlaceNoteDetailUi()

    data class SamePlaceNameItem(
        val placeNoteItems: List<PlaceNoteUiModel>
    ): PlaceNoteDetailUi()

    data class PurchaseNoteItem(
        val purchaseNoteItems: List<PurchaseNoteUiModel>
    ): PlaceNoteDetailUi()
}

sealed class PlaceNoteDetailUiState {
    data object Loading: PlaceNoteDetailUiState()

    data object NotExist: PlaceNoteDetailUiState()

    data class Success(
        val placeNoteItem: PlaceNoteUiModel? = null,
        val placeNoteDetailUiItems: List<PlaceNoteDetailUi> = emptyList()
    ): PlaceNoteDetailUiState()

    data class Error(val msg: String): PlaceNoteDetailUiState()
}

@HiltViewModel
class PlaceNoteDetailViewModel @Inject constructor(
    private val getPlaceNoteDetailByIdUseCase: GetPlaceNoteDetailByIdUseCase,
    private val deletePlaceNoteByIdUseCase: DeletePlaceNoteByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    val noteId = savedStateHandle.get<Int>(AppConstants.INTENT_NOTE_ID) ?: -1

    private val _uiState: MutableStateFlow<PlaceNoteDetailUiState> = MutableStateFlow(PlaceNoteDetailUiState.Success())
    val uiState = _uiState.asStateFlow()

    private val _requestDeleteEventState = MutableSharedFlow<UiState<Int>>()
    val requestDeleteEventState = _requestDeleteEventState.asSharedFlow()

    fun getPlaceNoteDetail() {
        viewModelScope.launch {
            getPlaceNoteDetailByIdUseCase(noteId)
                .collect { result -> result
                    .onLoading {
                        _uiState.value = PlaceNoteDetailUiState.Loading
                    }
                    .onError { throwable ->
                        if (throwable is NullPointerException) {
                            _uiState.value = PlaceNoteDetailUiState.NotExist
                        } else {
                            _uiState.value =
                                PlaceNoteDetailUiState.Error(throwable.message ?: "장소노트 상세정보 조회가 실패하였습니다.")
                        }
                    }
                    .onSuccess { data ->
                        val uiItems: MutableList<PlaceNoteDetailUi> = mutableListOf()

                        uiItems.add(PlaceNoteDetailUi.DetailItem(data.placeNote.toUiModel()))

                        if (data.purchaseNotes.isNotEmpty()) {
                            uiItems.add(PlaceNoteDetailUi.SectionTitleItem(sectionTitle = "구매노트가 있어요!"))
                            uiItems.add(PlaceNoteDetailUi.PurchaseNoteItem(purchaseNoteItems = data.purchaseNotes.toUiModel()))
                        }

                        if (data.samePlaceNameNotes.isNotEmpty()) {
                            uiItems.add(PlaceNoteDetailUi.SectionTitleItem(sectionTitle = "다른 날에도 방문했어요!"))
                            uiItems.add(PlaceNoteDetailUi.SamePlaceNameItem(placeNoteItems = data.samePlaceNameNotes.toUiModel()))
                        }

                        _uiState.value = PlaceNoteDetailUiState.Success(
                            placeNoteItem = data.placeNote.toUiModel(),
                            placeNoteDetailUiItems = uiItems
                        )
                    }
            }
        }
    }

    fun shownGetPlaceNoteDetailError() {
        _uiState.value = PlaceNoteDetailUiState.Success()
    }

    fun deletePlaceNote() {
        viewModelScope.launch {
            deletePlaceNoteByIdUseCase(noteId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _requestDeleteEventState.emit(UiState.Loading)
                    }
                    is Result.Error -> {
                        _requestDeleteEventState.emit(UiState.Error("장소노트 삭제가 실패했어요!"))
                    }
                    is Result.Success -> {
                        _requestDeleteEventState.emit(UiState.Success(result.data!!))
                    }
                }
            }
        }
    }
}
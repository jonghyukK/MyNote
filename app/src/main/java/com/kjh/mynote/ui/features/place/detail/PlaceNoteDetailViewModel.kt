package com.kjh.mynote.ui.features.place.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 19..
 * Description:
 */

data class PlaceNoteDetailUiState(
    val isLoading: Boolean = true,
    val placeNoteItem: PlaceNoteModel? = null,
    val placeNoteDetailUiItems: List<PlaceNoteDetailUi> = emptyList()
)

sealed class PlaceNoteDetailUi {

    data class DetailItem(
        val placeNoteItem: PlaceNoteModel
    ): PlaceNoteDetailUi()

    data class SamePlaceNameItem(
        val sectionTitle: String,
        val placeNoteItems: List<PlaceNoteModel>
    ): PlaceNoteDetailUi()
}

@HiltViewModel
class PlaceNoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    val noteId = savedStateHandle.get<Int>(AppConstants.INTENT_NOTE_ID) ?: -1

    private val _uiState = MutableStateFlow(PlaceNoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _deleteEvent = MutableSharedFlow<UiState<Unit>>()
    val deleteEvent = _deleteEvent.asSharedFlow()

    private val _itemNotExistEvent = MutableSharedFlow<Unit>()
    val itemNotExistEvent = _itemNotExistEvent.asSharedFlow()

    fun getPlaceNoteDetail() {
        viewModelScope.launch {
            noteRepository.getPlaceNoteWithSamePlaceNameNotes(noteId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.value = PlaceNoteDetailUiState(isLoading = true)
                    }
                    is Result.Error -> {
                        _itemNotExistEvent.emit(Unit)
                    }
                    is Result.Success -> {
                        result.data?.let { data ->
                            val (placeNoteItem, samePlaceNameNoteItems) = data

                            val uiItems: MutableList<PlaceNoteDetailUi> = mutableListOf()
                            uiItems.add(PlaceNoteDetailUi.DetailItem(placeNoteItem))

                            if (samePlaceNameNoteItems.isNotEmpty()) {
                                uiItems.add(
                                    PlaceNoteDetailUi.SamePlaceNameItem(
                                        sectionTitle = "다른 날에도 방문했어요!",
                                        placeNoteItems = samePlaceNameNoteItems
                                    )
                                )
                            }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    placeNoteItem = placeNoteItem,
                                    placeNoteDetailUiItems = uiItems
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun deletePlaceNote() {
        viewModelScope.launch {
            noteRepository.deletePlaceNoteById(noteId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _deleteEvent.emit(UiState.Loading)
                    }
                    is Result.Error -> {
                        _deleteEvent.emit(UiState.Error("장소노트 삭제가 실패했어요!"))
                    }
                    is Result.Success -> {
                        _deleteEvent.emit(UiState.Success(Unit))
                    }
                }
            }
        }
    }
}
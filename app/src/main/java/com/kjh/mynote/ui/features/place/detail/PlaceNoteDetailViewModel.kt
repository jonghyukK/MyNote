package com.kjh.mynote.ui.features.place.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
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
 * Created On 2024. 10. 19..
 * Description:
 */

data class PlaceNoteDetailUiState(
    val isLoading: Boolean = true,
    val placeNoteDetailUis: List<PlaceNoteDetailUi> = emptyList()
)

sealed class PlaceNoteDetailUi {

    data class PlaceNoteDetailItem(
        val placeNoteItem: PlaceNoteModel
    ): PlaceNoteDetailUi()
}

@HiltViewModel
class PlaceNoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val noteId =
        savedStateHandle.get<Int>(AppConstants.INTENT_NOTE_ID) ?: -1

    private val _uiState = MutableStateFlow(PlaceNoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _deleteEvent = MutableSharedFlow<Int>()
    val deleteEvent = _deleteEvent.asSharedFlow()

    fun getPlaceNote() {
        viewModelScope.launch {
            noteRepository.getPlaceNoteById(noteId = noteId)
                .collect { result ->
                    when (result) {
                        Result.Loading -> {}
                        is Result.Success -> {
                            result.data?.let { data ->

                                _uiState.value = PlaceNoteDetailUiState(
                                    isLoading = false,
                                    placeNoteDetailUis = listOf(
                                        PlaceNoteDetailUi.PlaceNoteDetailItem(data))
                                )
                            }
                        }
                        is Result.Error -> {}
                    }
                }
        }
    }

    fun deletePlaceNote() {
        viewModelScope.launch {
            noteRepository.deletePlaceNoteById(noteId).collect { result ->
                when (result) {
                    Result.Loading -> {}
                    is Result.Success -> {
                        result.data?.let { deletedNoteId ->
                            _deleteEvent.emit(result.data ?: deletedNoteId)
                        }
                    }
                    is Result.Error -> {}
                }
            }
        }
    }
}
package com.example.mynote.ui.features.note.make

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.data.model.Result
import com.example.data.repository.NoteRepository
import com.example.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MakeNoteUiState(
    val tempImageUris: List<Uri> = emptyList()
)

@HiltViewModel
class MakeNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _uiState = MutableStateFlow(MakeNoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _isSavedNote = MutableSharedFlow<Boolean>()
    val isSavedNote = _isSavedNote.asSharedFlow()

    fun saveNote(contents: String) {
        viewModelScope.launch {
            noteRepository.create(contents).collect { result ->
                when (result) {
                    Result.Loading -> {

                    }
                    is Result.Success -> {
                        _isSavedNote.emit(true)
                    }
                    is Result.Error -> {}
                }
            }
        }
    }

    fun setTempImageUris(uris: List<Uri>) {
        if (uris.isEmpty()) return

        _uiState.getAndUpdate { uiState ->
            val currentTempImageUris = uiState.tempImageUris
            val newTempImageUris = uris.filter { newUri ->
                newUri !in currentTempImageUris
            }

            uiState.copy(tempImageUris = currentTempImageUris + newTempImageUris)
        }
    }

    fun deleteTempImageByUri(targetUri: Uri?) {
        if (targetUri == null) return

        _uiState.update { uiState ->
            uiState.copy(
                tempImageUris = uiState.tempImageUris.filter { uri ->
                    uri != targetUri
                }
            )
        }
    }
}
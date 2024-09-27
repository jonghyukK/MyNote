package com.example.mynote.ui.features.note.list

import androidx.lifecycle.viewModelScope
import com.example.data.repository.NoteRepository
import com.example.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteListUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val noteItems: List<NoteItemUiState> = emptyList()
)

data class NoteItemUiState(
    val noteId: Int,
    val contents: String
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _uiState = MutableStateFlow<NoteListUiState>(NoteListUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchNoteItems() {
        viewModelScope.launch {
            noteRepository.observeAll().collect { notes ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = notes.isEmpty(),
                        noteItems = notes.map { note ->
                            NoteItemUiState(
                                noteId = note.id,
                                contents = note.contents
                            )
                        }
                    )
                }
            }
        }
    }
}
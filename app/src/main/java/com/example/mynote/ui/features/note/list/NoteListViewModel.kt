package com.example.mynote.ui.features.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val testNoteList = listOf(
    NoteItemUiState(contents = "안녕하세요1"),
    NoteItemUiState(contents = "안녕하세요2"),
    NoteItemUiState(contents = "안녕하세요3"),
    NoteItemUiState(contents = "안녕하세요4"),
    NoteItemUiState(contents = "안녕하세요5"),
    NoteItemUiState(contents = "안녕하세요6")
)

data class NoteListUiState(
    val isLoading: Boolean = true,
    val noteItems: List<NoteItemUiState> = emptyList()
)

data class NoteItemUiState(
    val contents: String
)

class NoteListViewModel: ViewModel() {

    private val _uiState = MutableStateFlow<NoteListUiState>(NoteListUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchNoteItems() {
        viewModelScope.launch {
            delay(2000)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    noteItems = testNoteList
                )
            }
        }
    }
}
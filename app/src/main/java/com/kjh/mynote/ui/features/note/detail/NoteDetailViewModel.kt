package com.kjh.mynote.ui.features.note.detail

import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.ui.features.note.list.NoteItemUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 26..
 * Description:
 */

data class NoteDetailUiState(
    val isLoading: Boolean = true,
    val noteItem: NoteItemUiState? = null
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun getNoteById(noteId: Int) {
//        viewModelScope.launch {
//            noteRepository.getNoteById(noteId).collect { result ->
//                when (result) {
//                    Result.Loading -> {
//                        _uiState.value = NoteDetailUiState(isLoading = true)
//                    }
//                    is Result.Success -> {
//                        result.data?.let { data ->
//                            _uiState.update {
//                                it.copy(
//                                    isLoading = false,
//                                    noteItem = NoteItemUiState(
//                                        noteId = data.id,
//                                        contents = data.contents
//                                    )
//                                )
//                            }
//                        }
//                    }
//                    is Result.Error -> {
//                        emitError(result.msg ?: "Occur Unknown Error")
//                        _uiState.value = NoteDetailUiState(isLoading = false)
//                    }
//                }
//            }
//        }
    }
}
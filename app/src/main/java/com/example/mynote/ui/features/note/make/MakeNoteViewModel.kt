package com.example.mynote.ui.features.note.make

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Result
import com.example.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MakeNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): ViewModel() {

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
}
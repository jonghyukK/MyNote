package com.example.mynote.ui.features.note.make

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.data.model.PlaceNoteModel
import com.example.data.model.Result
import com.example.data.repository.NoteRepository
import com.example.mynote.ui.base.BaseViewModel
import com.example.mynote.utils.constants.AppConstants
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

    fun savePlaceNote() {
        viewModelScope.launch {
            noteRepository.create(
                placeNoteModel = PlaceNoteModel(
                    placeImages = _uiState.value.tempImageUris.map { it.toString() },
                    description = "this is description",
                    placeName = "placeName",
                    placeAddress = "address",
                    x = 1000,
                    y = 2000,
                    startDate = 1000,
                    endDate = 3000
                )
            ).collect { result ->
                when (result) {
                    Result.Loading -> {
                        Log.e("Test123", "Loading")
                    }
                    is Result.Success -> {
                        Log.e("Test123", "Success")
                    }
                    is Result.Error -> {
                        Log.e("Test123", "Error: ${result.msg}")
                    }
                }
            }
        }
    }

    fun setTempImageUris(uris: List<Uri>) {
        if (uris.isEmpty()) return

        _uiState.update { uiState ->
            uiState.copy(
                tempImageUris = getValidTempImageUris(uiState.tempImageUris, uris)
            )
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

    private fun getValidTempImageUris(
        currentTempUris: List<Uri>,
        newTempUris: List<Uri>
    ): List<Uri> {
        val maxImageCount = AppConstants.MAX_SELECTABLE_IMAGE_COUNT
        val deduplicatedNewTempUris = newTempUris.filter { newUri ->
            newUri !in currentTempUris
        }

        val isOverMaxCount = (currentTempUris.size + deduplicatedNewTempUris.size) > maxImageCount
        if (isOverMaxCount) {
            val remainCount = maxImageCount - currentTempUris.size
            return currentTempUris + deduplicatedNewTempUris.subList(0, remainCount)
        } else {
            return currentTempUris + deduplicatedNewTempUris
        }
    }
}
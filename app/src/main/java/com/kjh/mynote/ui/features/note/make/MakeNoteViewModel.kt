package com.kjh.mynote.ui.features.note.make

import androidx.lifecycle.viewModelScope
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.repository.NoteRepository
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.utils.CalendarUtils
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
import javax.inject.Inject

data class MakeNoteUiState(
    val tempImageUrls: List<String> = emptyList(),
    val tempPlaceItem: KakaoPlaceModel? = null,
    val visitDate: Long = -1,
    val visitDateText: String = "",
    val title: String = "",
    val contents: String = ""
)

@HiltViewModel
class MakeNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): BaseViewModel() {

    private val _uiState = MutableStateFlow(MakeNoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _isSavedNoteEvent = MutableSharedFlow<UiState<Unit>>()
    val isSavedNoteEvent = _isSavedNoteEvent.asSharedFlow()

    val saveValidateFlow = _uiState.map {
        it.tempImageUrls.isNotEmpty()
                && it.tempPlaceItem != null
                && it.visitDate > 0
                && it.title.isNotBlank()
                && it.contents.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun savePlaceNote() {
        viewModelScope.launch {
            noteRepository.create(
                placeNoteModel = convertUiStateToPlaceNoteModel()
            ).collect { result ->
                when (result) {
                    Result.Loading -> {
                        _isSavedNoteEvent.emit(UiState.Loading)
                    }
                    is Result.Success -> {
                        result.data?.let {
                            _isSavedNoteEvent.emit(UiState.Success(Unit))
                        }
                    }
                    is Result.Error -> {
                        emitError("장소 노트 저장이 실패하였습니다.")
                    }
                }
            }
        }
    }

    fun setTempImages(imageUrls: List<String>) {
        if (imageUrls.isEmpty()) return

        _uiState.update { uiState ->
            uiState.copy(
                tempImageUrls = getValidTempImageUris(uiState.tempImageUrls, imageUrls)
            )
        }
    }

    fun deleteTempImageByUrl(targetUrl: String?) {
        if (targetUrl == null) return

        _uiState.update { uiState ->
            uiState.copy(
                tempImageUrls = uiState.tempImageUrls.filter { url ->
                    url != targetUrl
                }
            )
        }
    }

    fun setTempPlaceItem(placeItem: KakaoPlaceModel) {
        _uiState.update {
            it.copy(tempPlaceItem = placeItem)
        }
    }

    fun getTempPlaceItem() = _uiState.value.tempPlaceItem

    fun setVisitDate(timeInMills: Long) {
        _uiState.update {
            it.copy(
                visitDate = timeInMills,
                visitDateText = CalendarUtils.convertLongToDateFormat(
                    timeInMills,
                    "yyyy-MM-dd (E)"
                )
            )
        }
    }

    fun getVisitDateTimeMills() = _uiState.value.visitDate

    fun setTitle(title: String) {
        _uiState.update {
            it.copy(title = title)
        }
    }

    fun setContents(contents: String) {
        _uiState.update {
            it.copy(contents = contents)
        }
    }

    private fun convertUiStateToPlaceNoteModel() = with(_uiState.value) {
        PlaceNoteModel(
            placeImages = tempImageUrls,
            placeName = tempPlaceItem?.placeName ?: "",
            placeAddress = tempPlaceItem?.addressName ?: "",
            placeRoadAddress = tempPlaceItem?.roadAddressName ?: "",
            x = tempPlaceItem?.x ?: "",
            y = tempPlaceItem?.y ?: "",
            visitDate = visitDate,
            noteTitle = title,
            noteContents = contents
        )
    }

    private fun getValidTempImageUris(
        currentTempUris: List<String>,
        newTempUris: List<String>
    ): List<String> {
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
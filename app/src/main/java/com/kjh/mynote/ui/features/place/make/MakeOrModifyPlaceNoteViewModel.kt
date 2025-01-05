package com.kjh.mynote.ui.features.place.make

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.MakeAndGetPlaceNoteUseCase
import com.example.domain.usecase.MakeMultiplePurchaseNoteUseCase
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MakeOrModifyPlaceNoteViewModel @Inject constructor(
    private val makeMultiplePurchaseNotesUseCase: MakeMultiplePurchaseNoteUseCase,
    private val makeAndGetPlaceNoteUseCase: MakeAndGetPlaceNoteUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val initVisitDate =
        savedStateHandle.get<Long>(AppConstants.INTENT_PLACE_VISIT_DATE)

    private val initPlaceNoteItem =
        savedStateHandle.get<PlaceNoteUiModel>(AppConstants.INTENT_PLACE_NOTE_ITEM)

    private val _uiState = MutableStateFlow(MakeOrModifyNoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _upsertPlaceNoteEvent = MutableSharedFlow<UpsertPlaceNoteEventState>()
    val upsertPlaceNoteEvent = _upsertPlaceNoteEvent.asSharedFlow()

    val saveValidateFlow = _uiState.map {
        it.tempImageUrls.isNotEmpty()
                && it.tempPlaceItem != null
                && it.visitDate > 0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        initVisitDate?.let {
            setVisitDate(it)
        }

        initPlaceNoteItem?.let {
            setPlaceNoteItemForModifying(it)
        }
    }

    fun upsertPlaceNote() {
        if (_uiState.value.tempPurchaseNoteItems.isNotEmpty()) {
            upsertPlaceNoteWithPurchaseNotes()
            return
        }

        viewModelScope.launch {
            makeAndGetPlaceNoteUseCase(
                placeNote = _uiState.value.toDomainModel(),
                noteId = _uiState.value.noteId
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _upsertPlaceNoteEvent.emit(UpsertPlaceNoteEventState.Loading)
                    }
                    is ApiResult.Error -> {
                        val errorMsg = result.error.message ?: "장소노트 저장이 실패하였습니다."
                        _upsertPlaceNoteEvent.emit(UpsertPlaceNoteEventState.Error(errorMsg))
                    }
                    is ApiResult.Success -> {
                        val upsertPlaceNote = result.data.toUiModel()
                        _upsertPlaceNoteEvent.emit(UpsertPlaceNoteEventState.Success(upsertPlaceNote))
                    }
                }
            }
        }
    }

    private fun upsertPlaceNoteWithPurchaseNotes() {
        viewModelScope.launch {
            val placeNote = _uiState.value.toDomainModel()
            val purchaseNotes = _uiState.value.tempPurchaseNoteItems.toDomainModel()

            combine(
                makeMultiplePurchaseNotesUseCase(purchaseNotes),
                makeAndGetPlaceNoteUseCase(placeNote, _uiState.value.noteId)
            ) { makePurchaseNotesResult, makePlaceNoteResult ->
                when {
                    makePurchaseNotesResult is ApiResult.Loading ||
                            makePlaceNoteResult is ApiResult.Loading -> {
                        UpsertPlaceNoteEventState.Loading
                    }

                    makePurchaseNotesResult is ApiResult.Error -> {
                        val errorMsg = makePurchaseNotesResult.error.message ?: "구매노트 등록이 실패하였습니다."
                        UpsertPlaceNoteEventState.Error(errorMsg)
                    }

                    makePlaceNoteResult is ApiResult.Error -> {
                        val errorMsg = makePlaceNoteResult.error.message ?: "구매노트 등록이 실패하였습니다."
                        UpsertPlaceNoteEventState.Error(errorMsg)
                    }

                    makePurchaseNotesResult is ApiResult.Success &&
                            makePlaceNoteResult is ApiResult.Success -> {
                        val upsertPlaceNote = makePlaceNoteResult.data.toUiModel()
                        UpsertPlaceNoteEventState.Success(upsertPlaceNote)
                    }

                    else -> {
                        UpsertPlaceNoteEventState.Error("구매노트 등록이 실패하였습니다.")
                    }
                }
            }.collect { eventState ->
                _upsertPlaceNoteEvent.emit(eventState)
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

    fun setTempPlaceItem(placeItem: PlaceInfoUiModel) {
        _uiState.update {
            it.copy(tempPlaceItem = placeItem)
        }
    }

    fun setVisitDate(timeInMills: Long) {
        _uiState.update {
            it.copy(
                visitDate = timeInMills,
                visitDateText = timeInMills.toStringWithFormat("yyyy-MM-dd (E)")
            )
        }
    }

    fun getVisitDateTimeMills() = _uiState.value.visitDate

    fun setContents(contents: String) {
        _uiState.update {
            it.copy(contents = contents)
        }
    }

    fun addOrUpdatePurchaseNoteItem(tempPurchaseNoteItem: TempPurchaseNoteItem) {
        val currentTempPurchaseNoteItems = _uiState.value.tempPurchaseNoteItems.toMutableList()

        val existingIndex = currentTempPurchaseNoteItems.indexOfFirst { it.tempId == tempPurchaseNoteItem.tempId }
        if (existingIndex > -1) {
            currentTempPurchaseNoteItems[existingIndex] = tempPurchaseNoteItem
        } else {
            currentTempPurchaseNoteItems += tempPurchaseNoteItem
        }

        _uiState.update {
            it.copy(
                tempPurchaseNoteItems = currentTempPurchaseNoteItems
            )
        }
    }

    fun removeTempPurchaseNoteItem(tempPurchaseNoteItem: TempPurchaseNoteItem) {
        _uiState.update {
            it.copy(
                tempPurchaseNoteItems = it.tempPurchaseNoteItems - tempPurchaseNoteItem
            )
        }
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

    private fun setPlaceNoteItemForModifying(placeNoteModel: PlaceNoteUiModel) {
        _uiState.update {
            it.copy(
                noteId = placeNoteModel.id,
                tempImageUrls = placeNoteModel.placeImages,
                tempPlaceItem = placeNoteModel.placeInfo,
                visitDate = placeNoteModel.visitDate,
                visitDateText = placeNoteModel.visitDate.toStringWithFormat("yyyy-MM-dd (E)"),
                contents = placeNoteModel.noteContents,
            )
        }
    }

    private fun MakeOrModifyNoteUiState.toDomainModel() =
        PlaceNote(
            placeImages = tempImageUrls,
            placeInfo = tempPlaceItem!!.toDomainModel(),
            visitDate = visitDate,
            noteContents = contents
        )

    private fun List<TempPurchaseNoteItem>.toDomainModel() =
        _uiState.value.tempPurchaseNoteItems.map { tempItem ->
            PurchaseNote(
                purchaseDate = _uiState.value.visitDate,
                purchasePrice = tempItem.purchasePrice,
                purchaseName = tempItem.purchaseName,
                category = tempItem.categoryItem?.toDomainModel(),
                paymentMethod = tempItem.paymentMethod?.toDomainModal(),
                images = tempItem.tempImageUrls.ifEmpty { null },
                placeInfo = _uiState.value.tempPlaceItem?.toDomainModel()
            )
        }
}

sealed interface UpsertPlaceNoteEventState {
    data object Loading: UpsertPlaceNoteEventState
    data class Error(val errorMsg: String): UpsertPlaceNoteEventState
    data class Success(val placeNote: PlaceNoteUiModel): UpsertPlaceNoteEventState
}

data class MakeOrModifyNoteUiState(
    val noteId: Int = -1,
    val tempImageUrls: List<String> = emptyList(),
    val tempPlaceItem: PlaceInfoUiModel? = null,
    val visitDate: Long = -1,
    val visitDateText: String = "",
    val contents: String = "",
    val tempPurchaseNoteItems: List<TempPurchaseNoteItem> = emptyList()
)

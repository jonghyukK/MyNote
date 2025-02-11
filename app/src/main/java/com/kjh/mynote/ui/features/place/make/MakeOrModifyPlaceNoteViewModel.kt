package com.kjh.mynote.ui.features.place.make

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.GetPlaceNoteByIdUseCase
import com.example.domain.usecase.ObserveAllCategoriesUseCase
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.example.domain.usecase.UpsertAndGetPlaceNoteUseCase
import com.example.domain.usecase.UpsertPlaceNoteAndMakePurchaseNotesUseCase
import com.kjh.mynote.R
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toMillis
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MakeOrModifyPlaceNoteViewModel @Inject constructor(
    private val upsertAndGetPlaceNoteUseCase: UpsertAndGetPlaceNoteUseCase,
    private val getPlaceNoteByIdUseCase: GetPlaceNoteByIdUseCase,
    private val upsertPlaceNoteAndMakePurchaseNotesUseCase: UpsertPlaceNoteAndMakePurchaseNotesUseCase,
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val placeNoteId = savedStateHandle[AppConstants.INTENT_PLACE_NOTE_ID] ?: -1
    private val initVisitDate = savedStateHandle[AppConstants.INTENT_PLACE_VISIT_DATE] ?: LocalDate.now().toMillis()

    private val _uiState = MutableStateFlow(MakeOrModifyNoteUiState(
        visitDate = initVisitDate,
        visitDateText = initVisitDate.toStringWithFormat(DATE_PATTERN),
        titleRes = if (placeNoteId == -1) R.string.make_place_note else R.string.edit_place_note,
        bottomBtnTextRes = if (placeNoteId == -1) R.string.do_save else R.string.do_modify
    ))
    val uiState = _uiState.asStateFlow()

    private val allCategoriesFlow: StateFlow<List<CategoryUiModel>> = observeAllCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    private val allPaymentMethodsFlow: StateFlow<List<PaymentMethodUiModel>> = observeAllPaymentMethods()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    init {
        if (placeNoteId != -1) {
            fetchPlaceNote(placeNoteId)
        }

        observeCategoriesAndPaymentMethods()
    }

    fun requestUpsertPlaceNote() {
        if (_uiState.value.tempPurchaseNoteItems.isNotEmpty()) {
            upsertPlaceNoteAndMakePurchaseNotes()
            return
        }

        viewModelScope.launch {
            upsertAndGetPlaceNoteUseCase(
                placeNote = uiState.value.toDomainModel()
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        sendError(result.error.message)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                makeOrEditResult = result.data.toUiModel()
                            )
                        }
                    }
                }
            }
        }
    }

    fun setTempImages(imageUrls: List<String>) {
        _uiState.update { state ->
            state.copy(
                tempImageUrls = getValidTempImageUris(state.tempImageUrls, imageUrls)
            )
        }
    }

    fun deleteTempImageByUrl(targetUrl: String) {
        _uiState.update { state ->
            state.copy(
                tempImageUrls = state.tempImageUrls.filter { it != targetUrl }
            )
        }
    }

    fun setTempPlaceItem(placeItem: PlaceInfoUiModel) {
        _uiState.update { state ->
            state.copy(
                tempPlaceItem = placeItem
            )
        }
    }

    fun setVisitDate(timeInMills: Long) {
        _uiState.update { state ->
            state.copy(
                visitDate = timeInMills,
                visitDateText = timeInMills.toStringWithFormat(DATE_PATTERN)
            )
        }
    }

    fun setContents(contents: String) {
        _uiState.update { state ->
            state.copy(contents = contents)
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
            it.copy(tempPurchaseNoteItems = currentTempPurchaseNoteItems)
        }
    }

    fun removeTempPurchaseNoteItem(tempPurchaseNoteItem: TempPurchaseNoteItem) {
        _uiState.update {
            it.copy(tempPurchaseNoteItems = it.tempPurchaseNoteItems - tempPurchaseNoteItem)
        }
    }

    private fun fetchPlaceNote(placeNoteId: Int) {
        viewModelScope.launch {
            getPlaceNoteByIdUseCase(placeNoteId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        sendError(result.error.message)
                    }
                    is ApiResult.Success -> {
                        val noteItem = result.data?.toUiModel()
                        if (noteItem != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    noteId = noteItem.id,
                                    tempImageUrls = noteItem.placeImages,
                                    tempPlaceItem = noteItem.placeInfo,
                                    visitDate = noteItem.visitDate,
                                    visitDateText = noteItem.visitDate.toStringWithFormat(DATE_PATTERN),
                                    contents = noteItem.noteContents
                                )
                            }
                        } else {
                            sendError("존재하지 않는 장소노트입니다.")
                        }
                    }
                }
            }
        }
    }

    private fun upsertPlaceNoteAndMakePurchaseNotes() {
        viewModelScope.launch {
            val placeNoteDomainModel = _uiState.value.toDomainModel()
            val purchaseNotesDomainModel = _uiState.value.tempPurchaseNoteItems.toDomainModel()

            upsertPlaceNoteAndMakePurchaseNotesUseCase(
                placeNote = placeNoteDomainModel,
                purchaseNotes = purchaseNotesDomainModel
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        sendError(result.error.message)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                makeOrEditResult = result.data.toUiModel()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeCategoriesAndPaymentMethods() {
        viewModelScope.launch {
            combine(
                allPaymentMethodsFlow,
                allCategoriesFlow
            ) { paymentMethods, categories -> Pair(paymentMethods, categories) }
                .distinctUntilChanged()
                .collect { (paymentMethods, categories) ->
                    if (_uiState.value.tempPurchaseNoteItems.isEmpty()) return@collect

                    _uiState.update { uiState ->
                        val syncedNoteItems = uiState.tempPurchaseNoteItems.map { item ->
                            item.copy(
                                categoryItem = categories.firstOrNull { it.id == item.categoryItem?.id },
                                paymentMethod = paymentMethods.firstOrNull { it.paymentMethodId == item.paymentMethod?.paymentMethodId }
                            )
                        }

                        uiState.copy(tempPurchaseNoteItems = syncedNoteItems)
                    }
                }
        }
    }

    private fun observeAllPaymentMethods(): Flow<List<PaymentMethodUiModel>> =
        observeAllPaymentMethodsUseCase()
            .map { result ->
                when (result) {
                    is ApiResult.Loading -> emptyList()
                    is ApiResult.Error -> {
                        sendError(result.error.message)
                        emptyList()
                    }
                    is ApiResult.Success -> result.data.toUiModel()
                }
            }

    private fun observeAllCategories(): Flow<List<CategoryUiModel>> =
        observeAllCategoriesUseCase()
            .map { result ->
                when (result) {
                    is ApiResult.Loading -> emptyList()
                    is ApiResult.Error -> {
                        sendError(result.error.message)
                        emptyList()
                    }
                    is ApiResult.Success -> result.data.toUiModel()
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

    companion object {
        const val DATE_PATTERN = "yyyy-MM-dd (E)"
    }
}

data class MakeOrModifyNoteUiState(
    val isLoading: Boolean = false,
    val noteId: Int = -1,
    val tempImageUrls: List<String> = emptyList(),
    val tempPlaceItem: PlaceInfoUiModel? = null,
    val visitDate: Long = -1,
    val visitDateText: String = "",
    val contents: String = "",
    val tempPurchaseNoteItems: List<TempPurchaseNoteItem> = emptyList(),
    val titleRes: Int = R.string.make_place_note,
    val bottomBtnTextRes: Int = R.string.do_save,
    val makeOrEditResult: PlaceNoteUiModel? = null
) {
    fun toDomainModel() =
        PlaceNote(
            id = noteId,
            placeImages = tempImageUrls,
            placeInfo = tempPlaceItem!!.toDomainModel(),
            visitDate = visitDate,
            noteContents = contents
        )

    val canSaveOrEdit: Boolean
        get() {
            val isValidPlaceNote = tempImageUrls.isNotEmpty()
                    && tempPlaceItem != null
                    && visitDate > 0

            val isValidPurchaseNotes = if (tempPurchaseNoteItems.isNotEmpty()) {
                tempPurchaseNoteItems.all { it.isFulfillRequired }
            } else {
                true
            }

            return isValidPlaceNote && isValidPurchaseNotes
        }
}

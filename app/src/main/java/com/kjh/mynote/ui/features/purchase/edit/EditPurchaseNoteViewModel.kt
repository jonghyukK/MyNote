package com.kjh.mynote.ui.features.purchase.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchasePlaceInfo
import com.example.domain.model.Result
import com.example.domain.usecase.MakeAndGetPurchaseNoteUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.KakaoPlaceUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toStringWithFormat
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

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */

data class EditPurchaseNoteUiState(
    val purchaseName: String = "",
    val categoryItem: CategoryUiModel? = null,
    val purchaseDate: Long = -1,
    val purchaseDateText: String = "",
    val purchasePrice: Long = 0,
    val tempPlaceItem: KakaoPlaceUiModel? = null,
    val tempImageUrls: List<String> = emptyList(),
)

@HiltViewModel
class EditPurchaseNoteViewModel @Inject constructor(
    private val makeAndGetPurchaseNoteUseCase: MakeAndGetPurchaseNoteUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _purchaseNoteItem =
        savedStateHandle.getStateFlow<PurchaseNoteUiModel?>(AppConstants.INTENT_PURCHASE_NOTE_ITEM, null)

    private val _uiState = MutableStateFlow(EditPurchaseNoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _editPurchaseNoteEventState = MutableSharedFlow<UiState<PurchaseNoteUiModel>>()
    val editPurchaseNoteEventState = _editPurchaseNoteEventState.asSharedFlow()

    val editValidateFlow = _uiState.map {
        it.purchaseDate > 0
                && it.purchasePrice > 0
                && it.categoryItem != null
                && it.purchaseName.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        initData()
    }

    private fun initData() {
        _purchaseNoteItem.value?.let { item ->
            _uiState.value = item.toUiState()
        }
    }

    fun requestEditPurchaseNote() {
        viewModelScope.launch {
            makeAndGetPurchaseNoteUseCase(
                purchaseNote = convertUiStateToPurchaseNoteModel()
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _editPurchaseNoteEventState.emit(UiState.Loading)
                    }
                    is Result.Error -> {
                        _editPurchaseNoteEventState.emit(UiState.Error(result.msg ?: "구매노트 수정이 실패했어요."))
                    }
                    is Result.Success -> {
                        _editPurchaseNoteEventState.emit(UiState.Success(result.data!!.toUiModel()))
                    }
                }
            }
        }
    }

    fun setPurchaseName(name: String) {
        _uiState.update {
            it.copy(purchaseName = name)
        }
    }

    fun setCategoryItem(category: CategoryUiModel) {
        _uiState.update {
            it.copy(categoryItem = category)
        }
    }

    fun updateSelectedCategoryWhenChanged(category: CategoryUiModel) {
        val currentCategory = _uiState.value.categoryItem
        currentCategory?.let {
            if (it.id == category.id) {
                _uiState.update {
                    it.copy(categoryItem = category)
                }
            }
        }
    }

    fun deleteSelectedCategoryWhenChanged(categoryId: Int) {
        val currentCategory = _uiState.value.categoryItem
        currentCategory?.let {
            if (it.id == categoryId) {
                _uiState.update {
                    it.copy(categoryItem = null)
                }
            }
        }
    }

    fun setPurchasePrice(price: String) {
        _uiState.update {
            it.copy(purchasePrice = price.toLong())
        }
    }

    fun clearPurchasePrice() {
        _uiState.update {
            it.copy(purchasePrice = 0)
        }
    }

    fun setPurchaseDate(timeInMillis: Long) {
        _uiState.update {
            it.copy(
                purchaseDate = timeInMillis,
                purchaseDateText = timeInMillis.toStringWithFormat(DATE_PATTERN)
            )
        }
    }

    fun getPurchaseDateTimeMills() = _uiState.value.purchaseDate

    fun setTempPlaceItem(placeItem: KakaoPlaceUiModel) {
        _uiState.update {
            it.copy(tempPlaceItem = placeItem)
        }
    }

    fun getTempPlaceItem() = _uiState.value.tempPlaceItem

    fun setTempImages(imageUrls: List<String>) {
        if (imageUrls.isEmpty()) return

        _uiState.update {
            it.copy(
                tempImageUrls = getValidTempImageUris(it.tempImageUrls, imageUrls)
            )
        }
    }

    fun deleteTempImageByUrl(targetUrl: String?) {
        if (targetUrl == null) return

        _uiState.update { uiState ->
            uiState.copy(
                tempImageUrls = uiState.tempImageUrls.filter { it != targetUrl }
            )
        }
    }

    private fun PurchaseNoteUiModel.toUiState() =
        EditPurchaseNoteUiState(
            purchaseName = purchaseName,
            categoryItem = category,
            purchaseDate = purchaseDate,
            purchaseDateText = purchaseDate.toStringWithFormat(DATE_PATTERN),
            purchasePrice = purchasePrice,
            tempPlaceItem = purchasePlaceInfo?.let {
                KakaoPlaceUiModel(
                    placeName = it.placeName,
                    addressName = it.placeAddress,
                    roadAddressName = it.placeRoadAddress,
                    x = it.x,
                    y = it.y
                )
            },
            tempImageUrls = images ?: emptyList()
        )

    private fun convertUiStateToPurchaseNoteModel() = with (_uiState.value) {
        PurchaseNote(
            id = _purchaseNoteItem.value?.id ?: 0,
            purchaseDate = purchaseDate,
            purchasePrice = purchasePrice,
            purchaseName = purchaseName,
            category = categoryItem?.toDomainModel(),
            images = tempImageUrls.ifEmpty { null },
            purchasePlaceInfo = tempPlaceItem?.let {
                PurchasePlaceInfo(
                    placeName = it.placeName,
                    placeAddress = it.addressName,
                    placeRoadAddress = it.roadAddressName,
                    x = it.x,
                    y = it.y
                )
            }
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

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월 d일 (E)"
    }
}
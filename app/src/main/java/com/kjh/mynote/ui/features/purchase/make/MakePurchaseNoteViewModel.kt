package com.kjh.mynote.ui.features.purchase.make

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
 * Created On 2024. 11. 7..
 * Description:
 */

data class MakePurchaseNoteUiState(
    val purchaseName: String = "",
    val categoryItem: CategoryUiModel? = null,
    val purchaseDate: Long = -1,
    val purchaseDateText: String = "",
    val purchasePrice: Long = 0,
    val tempPlaceItem: KakaoPlaceUiModel? = null,
    val tempImageUrls: List<String> = emptyList()
)

@HiltViewModel
class MakePurchaseNoteViewModel @Inject constructor(
    private val makeAndGetPurchaseNoteUseCase: MakeAndGetPurchaseNoteUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(MakePurchaseNoteUiState())
    val uiState = _uiState.asStateFlow()

    private val _makePurchaseNoteEventState = MutableSharedFlow<UiState<PurchaseNoteUiModel>>()
    val makePurchaseNoteEventState = _makePurchaseNoteEventState.asSharedFlow()

    val saveValidateFlow = _uiState.map {
        it.purchaseDate > 0
                && it.purchasePrice > 0
                && it.categoryItem != null
                && it.purchaseName.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun makePurchaseNote() {
        viewModelScope.launch {
            makeAndGetPurchaseNoteUseCase(convertUiStateToPurchaseNoteModel()).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _makePurchaseNoteEventState.emit(UiState.Loading)
                    }
                    is Result.Error -> {
                        _makePurchaseNoteEventState.emit(UiState.Error(result.msg ?: ""))
                    }
                    is Result.Success -> {
                        _makePurchaseNoteEventState.emit(UiState.Success(result.data!!.toUiModel()))
                    }
                }
            }
        }
    }

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

    fun setTempPlaceItem(placeItem: KakaoPlaceUiModel) {
        _uiState.update {
            it.copy(tempPlaceItem = placeItem)
        }
    }

    fun getTempPlaceItem() = _uiState.value.tempPlaceItem

    fun setPurchaseDate(timeInMillis: Long) {
        _uiState.update {
            it.copy(
                purchaseDate = timeInMillis,
                purchaseDateText = timeInMillis.toStringWithFormat(DATE_PATTERN)
            )
        }
    }

    fun getVisitDateTimeMills() = _uiState.value.purchaseDate

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

    fun setPurchaseName(name: String) {
        _uiState.update {
            it.copy(purchaseName = name)
        }
    }

    fun setCategory(category: CategoryUiModel) {
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

    private fun convertUiStateToPurchaseNoteModel() = with(_uiState.value) {
        PurchaseNote(
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
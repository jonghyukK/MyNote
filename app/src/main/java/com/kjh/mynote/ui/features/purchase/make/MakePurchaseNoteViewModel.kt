package com.kjh.mynote.ui.features.purchase.make

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.MakeAndGetPurchaseNoteUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toDomainModal
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

@HiltViewModel
class MakePurchaseNoteViewModel @Inject constructor(
    private val makeAndGetPurchaseNoteUseCase: MakeAndGetPurchaseNoteUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val initDate = savedStateHandle.get<Long>(AppConstants.INTENT_PURCHASE_DATE)

    private val _uiState = MutableStateFlow(MakePurchaseNoteUiState(
        purchaseDate = initDate ?: -1,
        purchaseDateText = initDate?.toStringWithFormat(DATE_PATTERN) ?: ""
    ))
    val uiState = _uiState.asStateFlow()

    private val _makePurchaseNoteEventState = MutableSharedFlow<MakePurchaseNoteEventUiState>()
    val makePurchaseNoteEventState = _makePurchaseNoteEventState.asSharedFlow()

    val saveValidateFlow = _uiState.map {
        it.purchaseDate > 0
                && it.purchasePrice > 0
                && it.categoryItem != null
                && it.paymentMethod != null
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
                    is ApiResult.Loading -> {
                        _makePurchaseNoteEventState.emit(MakePurchaseNoteEventUiState.Loading)
                    }
                    is ApiResult.Error -> {
                        val errorMsg = result.error.message ?: "구매노트 생성이 실패하였습니다."
                        _makePurchaseNoteEventState.emit(MakePurchaseNoteEventUiState.Error(errorMsg))
                    }
                    is ApiResult.Success -> {
                        val madeItem = result.data.toUiModel()
                        _makePurchaseNoteEventState.emit(MakePurchaseNoteEventUiState.Success(madeItem))
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

    fun setTempPlaceItem(placeItem: PlaceInfoUiModel) {
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

    fun setCategory(category: CategoryUiModel?) {
        _uiState.update {
            it.copy(categoryItem = category)
        }
    }

    fun setPaymentMethod(paymentMethod: PaymentMethodUiModel?) {
        _uiState.update {
            it.copy(paymentMethod = paymentMethod)
        }
    }

    private fun convertUiStateToPurchaseNoteModel() = with(_uiState.value) {
        PurchaseNote(
            purchaseDate = purchaseDate,
            purchasePrice = purchasePrice,
            purchaseName = purchaseName,
            category = categoryItem?.toDomainModel(),
            paymentMethod = paymentMethod?.toDomainModal(),
            images = tempImageUrls.ifEmpty { null },
            placeInfo = tempPlaceItem?.toDomainModel()
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

sealed interface MakePurchaseNoteEventUiState {
    data object Loading: MakePurchaseNoteEventUiState
    data class Error(val errorMsg: String): MakePurchaseNoteEventUiState
    data class Success(val purchaseNoteItem: PurchaseNoteUiModel): MakePurchaseNoteEventUiState
}

data class MakePurchaseNoteUiState(
    val purchaseName: String = "",
    val categoryItem: CategoryUiModel? = null,
    val paymentMethod: PaymentMethodUiModel? = null,
    val purchaseDate: Long = -1,
    val purchaseDateText: String = "",
    val purchasePrice: Long = 0,
    val tempPlaceItem: PlaceInfoUiModel? = null,
    val tempImageUrls: List<String> = emptyList()
)
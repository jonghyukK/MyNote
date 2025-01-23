package com.kjh.mynote.ui.features.purchase.make

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.GetDefaultPaymentMethodUseCase
import com.example.domain.usecase.GetRecentPurchaseNamesByCategoryIdUseCase
import com.example.domain.usecase.MakeAndGetPurchaseNoteUseCase
import com.example.domain.usecase.UpdateDefaultPaymentAndMakePurchaseNoteUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toStringWithFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getRecentPurchaseNamesByCategoryIdUseCase: GetRecentPurchaseNamesByCategoryIdUseCase,
    private val getDefaultPaymentMethodUseCase: GetDefaultPaymentMethodUseCase,
    private val updateDefaultPaymentAndMakePurchaseNoteUseCase: UpdateDefaultPaymentAndMakePurchaseNoteUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val initDate = savedStateHandle.get<Long>(AppConstants.INTENT_PURCHASE_DATE)

    private val _uiState = MutableStateFlow(MakePurchaseNoteUiState(
            purchaseDate = initDate ?: -1,
            purchaseDateText = initDate?.toStringWithFormat(DATE_PATTERN) ?: ""
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _makePurchaseNoteEventState = MutableSharedFlow<MakePurchaseNoteEventState>()
    val makePurchaseNoteEventState = _makePurchaseNoteEventState.asSharedFlow()

    fun fetchDefaultPaymentMethod() {
        viewModelScope.launch {
            getDefaultPaymentMethodUseCase().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }
                    is ApiResult.Error -> {
                        sendError(result.error.message)

                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }

                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                paymentMethod = result.data?.toUiModel()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchRecentPurchaseNamesByCategoryId(categoryId: Int?) {
        viewModelScope.launch {
            getRecentPurchaseNamesByCategoryIdUseCase(categoryId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update {
                            it.copy(isLoading = true)
                        }
                    }
                    is ApiResult.Error -> {
                        sendError(result.error.message)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recentPurchaseNameItems = emptyList()
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recentPurchaseNameItems = result.data
                            )
                        }
                    }
                }
            }
        }
    }

    fun makePurchaseNote() {
        viewModelScope.launch {
            if (_uiState.value.isDefaultPaymentCheckBoxChecked) {
                updateDefaultPaymentMethodAndMakePurchaseNote()
                return@launch
            }

            makeAndGetPurchaseNoteUseCase(
                purchaseNote = _uiState.value.toDomainModel()
            ).collect(::handleMakePurchaseNoteResult)
        }
    }

    private suspend fun updateDefaultPaymentMethodAndMakePurchaseNote() {
        val paymentMethod = _uiState.value.paymentMethod?.copy(isDefault = true)?.toDomainModal() ?: return
        val purchaseNote = _uiState.value.toDomainModel()

        updateDefaultPaymentAndMakePurchaseNoteUseCase(
            paymentMethod, purchaseNote
        ).collect(::handleMakePurchaseNoteResult)
    }

    private suspend fun handleMakePurchaseNoteResult(result: ApiResult<PurchaseNote>) {
        when (result) {
            is ApiResult.Loading -> {
                _makePurchaseNoteEventState.emit(MakePurchaseNoteEventState.Loading)
            }
            is ApiResult.Error -> {
                sendError(result.error.message)
                _makePurchaseNoteEventState.emit(MakePurchaseNoteEventState.Error(result.error))
            }
            is ApiResult.Success -> {
                _makePurchaseNoteEventState.emit(MakePurchaseNoteEventState.Success(result.data.toUiModel()))
            }
        }
    }

    fun setTempImageUrls(urls: List<String>) {
        _uiState.update {
            it.copy(tempImageUrls = getValidTempImageUris(it.tempImageUrls, urls))
        }
    }

    fun deleteTempImageByUrl(url: String) {
        _uiState.update {
            it.copy(tempImageUrls = it.tempImageUrls.filter { it != url })
        }
    }

    fun setTempPlaceItem(placeItem: PlaceInfoUiModel) {
        _uiState.update {
            it.copy(tempPlaceItem = placeItem)
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
        if (category == _uiState.value.categoryItem) return
        if (category == null) {
            _uiState.update {
                it.copy(
                    categoryItem = null,
                    recentPurchaseNameItems = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(categoryItem = category)
            }
            fetchRecentPurchaseNamesByCategoryId(categoryId = category.id)
        }
    }

    fun setPaymentMethod(paymentMethod: PaymentMethodUiModel?) {
        if (paymentMethod == _uiState.value.paymentMethod) return

        _uiState.update {
            it.copy(
                paymentMethod = paymentMethod,
                isShowDefaultPaymentCheckBox = paymentMethod != null && !paymentMethod.isDefault,
                isDefaultPaymentCheckBoxChecked = false
            )
        }
    }

    fun toggleDefaultPaymentMethodChecked() {
        _uiState.update {
            it.copy(isDefaultPaymentCheckBoxChecked = !it.isDefaultPaymentCheckBoxChecked)
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

    companion object {
        private const val DATE_PATTERN = "yyyy년 M월 d일 (E)"
    }
}

sealed class MakePurchaseNoteEventState {
    data object Loading: MakePurchaseNoteEventState()
    data class Error(val error: Throwable): MakePurchaseNoteEventState()
    data class Success(val purchaseNoteItem: PurchaseNoteUiModel): MakePurchaseNoteEventState()
}

data class MakePurchaseNoteUiState(
    val isLoading: Boolean = false,
    val purchaseName: String = "",
    val categoryItem: CategoryUiModel? = null,
    val recentPurchaseNameItems: List<String> = emptyList(),
    val paymentMethod: PaymentMethodUiModel? = null,
    val purchaseDate: Long = -1,
    val purchaseDateText: String = "",
    val purchasePrice: Long = 0,
    val tempPlaceItem: PlaceInfoUiModel? = null,
    val tempImageUrls: List<String> = emptyList(),
    val isShowDefaultPaymentCheckBox: Boolean = false,
    val isDefaultPaymentCheckBoxChecked: Boolean = false
) {
    fun toDomainModel(): PurchaseNote =
        PurchaseNote(
            purchaseDate = purchaseDate,
            purchasePrice = purchasePrice,
            purchaseName = purchaseName,
            category = categoryItem?.toDomainModel(),
            paymentMethod = paymentMethod?.toDomainModal(),
            images = tempImageUrls.ifEmpty { null },
            placeInfo = tempPlaceItem?.toDomainModel()
        )

    val canSave: Boolean =
        purchaseDate > 0
                && purchasePrice > 0
                && categoryItem != null
                && paymentMethod != null
                && purchaseName.isNotBlank()
}
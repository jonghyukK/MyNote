package com.kjh.mynote.ui.features.purchase.makeedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.GetDefaultPaymentMethodUseCase
import com.example.domain.usecase.GetPurchaseNoteByIdUseCase
import com.example.domain.usecase.GetRecentPurchaseNamesByCategoryIdUseCase
import com.example.domain.usecase.UpdateDefaultPaymentAndMakePurchaseNoteUseCase
import com.example.domain.usecase.UpsertAndGetPurchaseNoteUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
class MakeEditPurchaseNoteViewModel @Inject constructor(
    private val getPurchaseNoteByIdUseCase: GetPurchaseNoteByIdUseCase,
    private val upsertAndGetPurchaseNoteUseCase: UpsertAndGetPurchaseNoteUseCase,
    private val getRecentPurchaseNamesByCategoryIdUseCase: GetRecentPurchaseNamesByCategoryIdUseCase,
    private val getDefaultPaymentMethodUseCase: GetDefaultPaymentMethodUseCase,
    private val updateDefaultPaymentAndMakePurchaseNoteUseCase: UpdateDefaultPaymentAndMakePurchaseNoteUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val purchaseNoteId: Int? = savedStateHandle.get<Int>(AppConstants.INTENT_PURCHASE_NOTE_ID)
    private val initDate: Long? = savedStateHandle.get<Long>(AppConstants.INTENT_PURCHASE_DATE)

    private val _uiState = MutableStateFlow(MakePurchaseNoteUiState(
        viewType = purchaseNoteId?.let { ViewType.EDIT } ?: ViewType.MAKE,
        purchaseDate = initDate,
    ))
    val uiState = _uiState.asStateFlow()

    fun initialize() {
        if (purchaseNoteId == null) {
            fetchDefaultPaymentMethod()
        } else {
            fetchPurchaseNote()
        }
    }

    fun makeOrEditPurchaseNote() {
        val paymentMethodDomainModel =
            _uiState.value.paymentMethodItem?.copy(isDefault = true)?.toDomainModal() ?: run {
                sendError("makePurchaseNote() was called but paymentMethod is null")
                return
            }

        var purchaseNoteDomainModel = _uiState.value.toDomainModel()
        if (_uiState.value.viewType == ViewType.EDIT) {
            purchaseNoteDomainModel = purchaseNoteDomainModel.copy(id = purchaseNoteId!!)
        }

        if (_uiState.value.isDefaultPaymentCheckBoxChecked) {
            updateDefaultPaymentMethodAndMakePurchaseNote(
                paymentMethodDomainModel = paymentMethodDomainModel,
                purchaseNoteDomainModel = purchaseNoteDomainModel
            )
            return
        }

        viewModelScope.launch {
            upsertAndGetPurchaseNoteUseCase(purchaseNote = purchaseNoteDomainModel)
                .collect(::handleMakePurchaseNoteResult)
        }
    }

    fun setCategory(category: CategoryUiModel?) {
        if (_uiState.value.categoryItem == category) return

        viewModelScope.launch {
            val recentPurchaseNames = category?.id?.let { categoryId ->
                fetchRecentPurchaseNames(categoryId)
            } ?: emptyList()

            _uiState.update {
                it.copy(
                    categoryItem = category,
                    recentPurchaseNameItems = recentPurchaseNames
                )
            }
        }
    }

    fun setPurchaseDate(timeInMillis: Long) {
        _uiState.update {
            it.copy(purchaseDate = timeInMillis)
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

    fun setPaymentMethod(paymentMethod: PaymentMethodUiModel?) {
        if (_uiState.value.paymentMethodItem == paymentMethod) return
        _uiState.update {
            it.copy(
                paymentMethodItem = paymentMethod,
                isDefaultPaymentCheckBoxChecked = false
            )
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

    fun toggleDefaultPaymentMethodChecked() {
        _uiState.update {
            it.copy(isDefaultPaymentCheckBoxChecked = !it.isDefaultPaymentCheckBoxChecked)
        }
    }

    private fun fetchPurchaseNote() {
        viewModelScope.launch {
            getPurchaseNoteByIdUseCase(purchaseNoteId!!).collect { result ->
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
                        val purchaseNote = result.data

                        val recentPurchaseNames = purchaseNote.category?.id?.let { categoryId ->
                            fetchRecentPurchaseNames(categoryId)
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categoryItem = purchaseNote.category?.toUiModel(),
                                paymentMethodItem = purchaseNote.paymentMethod?.toUiModel(),
                                purchaseName = purchaseNote.purchaseName,
                                recentPurchaseNameItems = recentPurchaseNames,
                                purchaseDate = purchaseNote.purchaseDate,
                                purchasePrice = purchaseNote.purchasePrice,
                                tempPlaceItem = purchaseNote.placeInfo?.toUiModel(),
                                tempImageUrls = purchaseNote.images ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchRecentPurchaseNames(categoryId: Int): List<String> {
        var recentNames: List<String> = emptyList()

        getRecentPurchaseNamesByCategoryIdUseCase(categoryId).collect { result ->
            when (result) {
                is ApiResult.Loading -> {
                    recentNames = emptyList()
                }
                is ApiResult.Error -> {
                    sendError(result.error.message)
                    recentNames = emptyList()
                }
                is ApiResult.Success -> {
                    recentNames = result.data
                }
            }
        }

        return recentNames
    }

    private fun fetchDefaultPaymentMethod() {
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
                                paymentMethodItem = result.data?.toUiModel()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateDefaultPaymentMethodAndMakePurchaseNote(
        paymentMethodDomainModel: PaymentMethod,
        purchaseNoteDomainModel: PurchaseNote
    ) {
        viewModelScope.launch {
            updateDefaultPaymentAndMakePurchaseNoteUseCase(
                paymentMethod = paymentMethodDomainModel,
                purchaseNote = purchaseNoteDomainModel
            ).collect(::handleMakePurchaseNoteResult)
        }
    }

    private fun handleMakePurchaseNoteResult(result: ApiResult<PurchaseNote>) {
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
                        makeEditResult = result.data.toUiModel()
                    )
                }
            }
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
}

enum class ViewType {
    MAKE,
    EDIT
}

data class MakePurchaseNoteUiState(
    val isLoading: Boolean = false,
    val viewType: ViewType = ViewType.MAKE,
    val categoryItem: CategoryUiModel? = null,
    val purchaseName: String = "",
    val purchasePrice: Long = 0,
    val purchaseDate: Long? = null,
    val paymentMethodItem: PaymentMethodUiModel? = null,
    val tempPlaceItem: PlaceInfoUiModel? = null,
    val tempImageUrls: List<String> = emptyList(),
    val recentPurchaseNameItems: List<String> = emptyList(),
    val isDefaultPaymentCheckBoxChecked: Boolean = false,
    val makeEditResult: PurchaseNoteUiModel? = null
) {
    fun toDomainModel(): PurchaseNote =
        PurchaseNote(
            purchaseDate = purchaseDate!!,
            purchasePrice = purchasePrice,
            purchaseName = purchaseName,
            category = categoryItem?.toDomainModel(),
            paymentMethod = paymentMethodItem?.toDomainModal(),
            images = tempImageUrls.ifEmpty { null },
            placeInfo = tempPlaceItem?.toDomainModel()
        )

    val canSave: Boolean =
        purchaseDate != null &&
                purchaseDate > 0
                && purchasePrice > 0
                && categoryItem != null
                && paymentMethodItem != null
                && purchaseName.isNotBlank()

    val shouldShowDefaultPaymentCheckBox = paymentMethodItem != null &&
            !paymentMethodItem.isDefault
}
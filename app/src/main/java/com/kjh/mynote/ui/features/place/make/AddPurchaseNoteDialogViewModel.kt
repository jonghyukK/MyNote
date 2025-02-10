package com.kjh.mynote.ui.features.place.make

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.GetDefaultPaymentMethodUseCase
import com.example.domain.usecase.GetRecentPurchaseNamesByCategoryIdUseCase
import com.example.domain.usecase.UpsertPaymentMethodUseCase
import com.kjh.mynote.R
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.ui.features.place.make.AddPurchaseNoteDialogFragment.Companion.ARG_OBJ_PURCHASE_NOTE_ITEM
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

@HiltViewModel
class AddPurchaseNoteDialogViewModel @Inject constructor(
    private val getRecentPurchaseNamesByCategoryIdUseCase: GetRecentPurchaseNamesByCategoryIdUseCase,
    private val getDefaultPaymentMethodUseCase: GetDefaultPaymentMethodUseCase,
    private val upsertPaymentMethodUseCase: UpsertPaymentMethodUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val initTempPurchaseNoteItem: TempPurchaseNoteItem? = savedStateHandle[ARG_OBJ_PURCHASE_NOTE_ITEM]

    private val _uiState = MutableStateFlow(AddPurchaseNoteDialogUiState(
        tempPurchaseNoteItem = initTempPurchaseNoteItem ?: TempPurchaseNoteItem(),
        titleRes = if (initTempPurchaseNoteItem == null) R.string.add_purchase_note else R.string.purchase_note_edit,
        bottomBtnTextRes = if (initTempPurchaseNoteItem == null) R.string.do_add else R.string.do_modify
    ))
    val uiState = _uiState.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            if (initTempPurchaseNoteItem == null) {
                fetchDefaultPaymentMethod()
            } else {
                val categoryId = initTempPurchaseNoteItem.categoryItem?.id
                _uiState.update {
                    it.copy(recentPurchaseNames = getRecentPurchaseNames(categoryId))
                }
            }
        }
    }

    fun updateDefaultPaymentMethod() {
        viewModelScope.launch {
            val paymentMethodItem =
                _uiState.value.tempPurchaseNoteItem.paymentMethod?.copy(isDefault = true)
                    ?: return@launch

            upsertPaymentMethodUseCase(paymentMethodItem.toDomainModal()).collect { result ->
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
                            val updatedTempItem = it.tempPurchaseNoteItem.copy(
                                paymentMethod = it.tempPurchaseNoteItem.paymentMethod?.copy(isDefault = true)
                            )

                            it.copy(
                                isLoading = false,
                                tempPurchaseNoteItem = updatedTempItem,
                                isUpdatedDefaultPaymentMethod = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun setTempImages(imageUrls: List<String>) {
        _uiState.update { uiState ->
            uiState.copy(
                tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                    tempImageUrls = getValidTempImageUris(
                        uiState.tempPurchaseNoteItem.tempImageUrls,
                        imageUrls
                    )
                )
            )
        }
    }

    fun deleteTempImageByUrl(targetUrl: String) {
        _uiState.update { uiState ->
            uiState.copy(
                tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                    tempImageUrls = uiState.tempPurchaseNoteItem.tempImageUrls.filter { it != targetUrl }
                )
            )
        }
    }

    fun setPurchasePrice(price: String) {
        _uiState.update { uiState ->
            uiState.copy(
                tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                    purchasePrice = price.toLong()
                )
            )
        }
    }

    fun clearPurchasePrice() {
        _uiState.update { uiState ->
            uiState.copy(
                tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                    purchasePrice = 0
                )
            )
        }
    }

    fun setPurchaseName(name: String) {
        _uiState.update { uiState ->
            uiState.copy(
                tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                    purchaseName = name
                )
            )
        }
    }

    fun setCategory(category: CategoryUiModel?) {
        viewModelScope.launch {
            val recentPurchaseNames = category?.id?.let {
                getRecentPurchaseNames(it)
            } ?: emptyList()

            _uiState.update { uiState ->
                uiState.copy(
                    tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                        categoryItem = category
                    ),
                    recentPurchaseNames = recentPurchaseNames
                )
            }
        }
    }

    fun setPaymentMethod(paymentMethod: PaymentMethodUiModel?) {
        if (paymentMethod == _uiState.value.tempPurchaseNoteItem.paymentMethod) return

        _uiState.update { uiState ->
            uiState.copy(
                tempPurchaseNoteItem = uiState.tempPurchaseNoteItem.copy(
                    paymentMethod = paymentMethod
                ),
                isDefaultPaymentCheckBoxChecked = false
            )
        }
    }

    fun toggleDefaultPaymentMethodChecked() {
        _uiState.update { uiState ->
            uiState.copy(
                isDefaultPaymentCheckBoxChecked = !uiState.isDefaultPaymentCheckBoxChecked
            )
        }
    }

    private suspend fun getRecentPurchaseNames(categoryId: Int?): List<String> {
        if (categoryId == null) return emptyList()

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

    private suspend fun fetchDefaultPaymentMethod() {
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
                            tempPurchaseNoteItem = it.tempPurchaseNoteItem.copy(
                                paymentMethod = result.data?.toUiModel()
                            )
                        )
                    }
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

@Parcelize
data class TempPurchaseNoteItem(
    val tempId: Long = System.currentTimeMillis(),
    val categoryItem: CategoryUiModel? = null,
    val purchaseName: String = "",
    val purchasePrice: Long = 0L,
    val paymentMethod: PaymentMethodUiModel? = null,
    val tempImageUrls: List<String> = emptyList()
): Parcelable {
    @IgnoredOnParcel
    val isFulfillRequired = categoryItem != null
            && purchaseName.isNotBlank()
            && purchasePrice > 0
            && paymentMethod != null
}

data class AddPurchaseNoteDialogUiState(
    val isLoading: Boolean = false,
    val tempPurchaseNoteItem: TempPurchaseNoteItem = TempPurchaseNoteItem(),
    val recentPurchaseNames: List<String> = emptyList(),
    val isDefaultPaymentCheckBoxChecked: Boolean = false,
    val isUpdatedDefaultPaymentMethod: Boolean = false,
    val titleRes: Int = R.string.add_purchase_note,
    val bottomBtnTextRes: Int = R.string.do_add
) {
    val shouldShowDefaultPaymentCheckBox = tempPurchaseNoteItem.paymentMethod != null &&
            !tempPurchaseNoteItem.paymentMethod.isDefault
}
package com.kjh.mynote.ui.features.place.make

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.GetRecentPurchaseNamesByCategoryIdUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui.features.purchase.make.RecentPurchaseNamesUiState
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

@Parcelize
data class TempPurchaseNoteItem(
    val tempId: Long = System.currentTimeMillis(),
    val purchasePrice: Long = 0L,
    val purchaseName: String = "",
    val categoryItem: CategoryUiModel? = null,
    val paymentMethod: PaymentMethodUiModel? = null,
    val tempImageUrls: List<String> = emptyList()
): Parcelable

@HiltViewModel
class AddPurchaseNoteDialogViewModel @Inject constructor(
    private val getRecentPurchaseNamesByCategoryIdUseCase: GetRecentPurchaseNamesByCategoryIdUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val tempPurchaseNoteItem: StateFlow<TempPurchaseNoteItem?> =
        savedStateHandle.getStateFlow(AppConstants.INTENT_PURCHASE_NOTE_ITEM, null)

    private val _uiState = MutableStateFlow(tempPurchaseNoteItem.value ?: TempPurchaseNoteItem())
    val uiState = _uiState.asStateFlow()

    val bottomBtnValidateFlow = _uiState.map {
        it.purchasePrice > 0
                && it.categoryItem != null
                && it.paymentMethod != null
                && it.purchaseName.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val recentRegisteredPurchaseNamesUiState = uiState
        .map { it.categoryItem }
        .distinctUntilChanged()
        .flatMapLatest { category ->
            getRecentPurchaseNamesByCategoryIdUseCase(category?.id)
                .map(::mapRecentPurchaseNamesResultToUiState)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RecentPurchaseNamesUiState.Wait
        )

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

    private fun mapRecentPurchaseNamesResultToUiState(result: ApiResult<List<String>>) =
        when (result) {
            is ApiResult.Loading ->
                RecentPurchaseNamesUiState.Loading

            is ApiResult.Error ->
                RecentPurchaseNamesUiState.Error(
                    result.error.message ?: "최근 등록한 구매명 목록 조회가 실패하였습니다."
                )

            is ApiResult.Success ->
                RecentPurchaseNamesUiState.Success(result.data)
        }
}
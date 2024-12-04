package com.kjh.mynote.ui.features.place.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

data class TempPurchaseNoteItem(
    val tempId: Long = System.currentTimeMillis(),
    val purchasePrice: Long = 0L,
    val purchaseName: String = "",
    val categoryItem: CategoryUiModel? = null,
    val tempImageUrls: List<String> = emptyList()
)

@HiltViewModel
class AddPurchaseNoteDialogViewModel @Inject constructor(

): ViewModel() {

    private val _uiState = MutableStateFlow(TempPurchaseNoteItem())
    val uiState = _uiState.asStateFlow()

    val bottomBtnValidateFlow = _uiState.map {
        it.purchasePrice > 0
                && it.categoryItem != null
                && it.purchaseName.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setInitItem(item: TempPurchaseNoteItem?) {
        item?.let {
            _uiState.value = item
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
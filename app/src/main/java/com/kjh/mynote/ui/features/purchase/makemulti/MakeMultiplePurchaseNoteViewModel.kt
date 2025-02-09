package com.kjh.mynote.ui.features.purchase.makemulti

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.usecase.GetRecentPurchaseNamesByCategoryIdUseCase
import com.example.domain.usecase.MakeMultiplePurchaseNoteUseCase
import com.example.domain.usecase.ObserveAllCategoriesUseCase
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.toDomainModal
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 3..
 * Description:
 */

@HiltViewModel
class MakeMultiplePurchaseNoteViewModel @Inject constructor(
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase,
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    private val getRecentPurchaseNamesUseCase: GetRecentPurchaseNamesByCategoryIdUseCase,
    private val makeMultiplePurchaseNotesUseCase: MakeMultiplePurchaseNoteUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val initDate: Long = savedStateHandle.get<Long>(AppConstants.INTENT_PURCHASE_DATE) ?: LocalDate.now().toMillis()

    private val _uiState = MutableStateFlow(MakeMultiplePurchaseNoteUiState(
        tempPurchaseNoteItems = listOf(TempPurchaseNoteUiState(purchaseDate = initDate))
    ))
    val uiState = _uiState.asStateFlow()

    private val allCategoriesFlow: StateFlow<List<CategoryUiModel>> = observeAllCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val allPaymentMethodsFlow: StateFlow<List<PaymentMethodUiModel>> = observePaymentMethods()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        observeCategoriesAndPaymentMethods(allCategoriesFlow, allPaymentMethodsFlow)
    }

   fun requestMakeMultiplePurchaseNotes() {
       viewModelScope.launch {
           val tempPurchaseNoteDomainModels = _uiState.value.tempPurchaseNoteItems.map { it.toDomainModel() }

           makeMultiplePurchaseNotesUseCase(tempPurchaseNoteDomainModels).collect { result ->
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
                               isMadeSuccess = true
                           )
                       }
                   }
               }
           }
       }
   }

    fun setCategory(tempId: Int, categoryItem: CategoryUiModel?) {
        viewModelScope.launch {
            val recentPurchaseNames = categoryItem?.id?.let { categoryId ->
                fetchRecentPurchaseNames(categoryId)
            } ?: emptyList()

            updateTempPurchaseNoteItem(tempId) {
                it.copy(
                    categoryItem = categoryItem,
                    recentPurchaseNameItems = recentPurchaseNames
                )
            }
        }
    }

    fun setPurchaseName(tempId: Int, purchaseName: String, isAutoPurchaseNameSetMode: Boolean) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(
                purchaseName = purchaseName,
                isAutoPurchaseNameSetMode = isAutoPurchaseNameSetMode
            )
        }
    }

    fun setPurchasePrice(tempId: Int, purchasePrice: Long) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(purchasePrice = purchasePrice)
        }
    }

    fun setPurchaseDate(tempId: Int, date: Long) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(purchaseDate = date)
        }
    }

    fun setPaymentMethod(tempId: Int, paymentMethodItem: PaymentMethodUiModel?) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(paymentMethodItem = paymentMethodItem)
        }
    }

    fun setPlaceItem(tempId: Int, placeItem: PlaceInfoUiModel) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(tempPlaceItem = placeItem)
        }
    }

    fun setTempImageUrls(tempId: Int, urls: List<String>) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(tempImageUrls = getValidTempImageUris(it.tempImageUrls, urls))
        }
    }

    fun deleteTempImageByUrl(tempId: Int, targetUrl: String) {
        updateTempPurchaseNoteItem(tempId) {
            it.copy(tempImageUrls = it.tempImageUrls.filter { it != targetUrl })
        }
    }

    fun addNextItem() {
        val tempItems = _uiState.value.tempPurchaseNoteItems.toMutableList()
        if (tempItems.isNotEmpty()) {
            tempItems[tempItems.lastIndex] = tempItems.last().copy(
                isLastItem = false,
                showDeleteButton = true
            )
        }

        tempItems.add(
            TempPurchaseNoteUiState(
                tempId = (tempItems.lastOrNull()?.tempId ?: 0) + 1,
                tempIndex = tempItems.size + 1,
                purchaseDate = initDate,
                paymentMethodItem = _uiState.value.defaultPaymentMethod,
                showDeleteButton = tempItems.isNotEmpty()
            )
        )

        _uiState.update {
            it.copy(tempPurchaseNoteItems = tempItems)
        }
    }

    fun deleteItem(tempId: Int) {
        val filteredItems = _uiState.value.tempPurchaseNoteItems
            .filter { it.tempId != tempId }

        val newItems = filteredItems.mapIndexed { index, tempItem ->
            tempItem.copy(
                tempIndex = index + 1,
                isLastItem = index == filteredItems.lastIndex,
                showDeleteButton = filteredItems.size > 1
            )
        }

        _uiState.update {
            it.copy(tempPurchaseNoteItems = newItems)
        }
    }

    private fun updateTempPurchaseNoteItem(
        tempId: Int,
        updateAction: (TempPurchaseNoteUiState) -> TempPurchaseNoteUiState
    ) {
        val tempItems = _uiState.value.tempPurchaseNoteItems.toMutableList()
        val tempItemIndex = tempItems.indexOfFirst { it.tempId == tempId }
        if (tempItemIndex == -1) return

        tempItems[tempItemIndex] = updateAction(tempItems[tempItemIndex])

        _uiState.update {
            it.copy(tempPurchaseNoteItems = tempItems)
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

    private fun observeCategoriesAndPaymentMethods(
        allCategoriesFlow: Flow<List<CategoryUiModel>>,
        allPaymentMethodsFlow: Flow<List<PaymentMethodUiModel>>
    ) {
        viewModelScope.launch {
            combine(
                allCategoriesFlow,
                allPaymentMethodsFlow
            ) { categories, paymentMethods ->
                val defaultPaymentMethodItem = paymentMethods.find { it.isDefault }

                _uiState.value.copy(
                    tempPurchaseNoteItems = _uiState.value.tempPurchaseNoteItems.map { tempItem ->
                        val matchedCategoryItem = categories.find { it.id == tempItem.categoryItem?.id }
                        val matchedPaymentMethodItem = paymentMethods.find {
                            it.paymentMethodId == tempItem.paymentMethodItem?.paymentMethodId }

                        tempItem.copy(
                            categoryItem = matchedCategoryItem,
                            paymentMethodItem = matchedPaymentMethodItem ?: defaultPaymentMethodItem
                        )
                    },
                    defaultPaymentMethod = defaultPaymentMethodItem
                )
            }.collect { updatedState ->
                _uiState.update { updatedState }
            }
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

    private fun observePaymentMethods(): Flow<List<PaymentMethodUiModel>> =
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

    private suspend fun fetchRecentPurchaseNames(categoryId: Int): List<String> {
        var recentNames: List<String> = emptyList()

        getRecentPurchaseNamesUseCase(categoryId).collect { result ->
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
}

data class TempPurchaseNoteUiState(
    val tempId: Int = 1,
    val tempIndex: Int = 1,
    val categoryItem: CategoryUiModel? = null,
    val recentPurchaseNameItems: List<String> = emptyList(),
    val purchaseName: String = "",
    val isAutoPurchaseNameSetMode: Boolean = false,
    val purchasePrice: Long = 0,
    val purchaseDate: Long = LocalDate.now().toMillis(),
    val paymentMethodItem: PaymentMethodUiModel? = null,
    val tempPlaceItem: PlaceInfoUiModel? = null,
    val tempImageUrls: List<String> = emptyList(),
    val isLastItem: Boolean = true,
    val showDeleteButton: Boolean = false
) {
    fun toDomainModel() = PurchaseNote(
        purchaseDate = purchaseDate,
        purchasePrice = purchasePrice,
        purchaseName = purchaseName,
        category = categoryItem?.toDomainModel(),
        paymentMethod = paymentMethodItem?.toDomainModal(),
        images = tempImageUrls.ifEmpty { null },
        placeInfo = tempPlaceItem?.toDomainModel()
    )
}

data class MakeMultiplePurchaseNoteUiState(
    val isLoading: Boolean = false,
    val tempPurchaseNoteItems: List<TempPurchaseNoteUiState> = emptyList(),
    val defaultPaymentMethod: PaymentMethodUiModel? = null,
    val isMadeSuccess: Boolean = false
) {
    val canSave: Boolean = tempPurchaseNoteItems.all {
        it.categoryItem != null &&
                it.purchaseName.isNotBlank() &&
                it.purchasePrice > 0 &&
                it.purchaseDate > 0 &&
                it.paymentMethodItem != null
    }
}
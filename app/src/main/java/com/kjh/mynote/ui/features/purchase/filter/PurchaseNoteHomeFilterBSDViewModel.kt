package com.kjh.mynote.ui.features.purchase.filter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.example.domain.usecase.GetPaymentMethodsUseCase
import com.kjh.mynote.model.Filters
import com.kjh.mynote.model.getAppliedCategoryIds
import com.kjh.mynote.model.getAppliedPaymentMethodIds
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */

@HiltViewModel
class PurchaseNoteHomeFilterBSDViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getAllPaymentMethodsUseCase: GetPaymentMethodsUseCase,
    val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _initAppliedFilters = MutableStateFlow<List<Filters>>(emptyList())
    val initAppliedFilters = _initAppliedFilters.asStateFlow()

    private val _uiState = MutableStateFlow(PurchaseNoteHomeFilterUiState())
    val uiState = _uiState.asStateFlow()

    val isChangedFilterFlow = combine(
        _initAppliedFilters, _uiState
    ) { initFilters, uiState ->
        initFilters != uiState.selectedFilterItems()
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun getFilterItems() {
        viewModelScope.launch {
            combine(
                getAllCategoriesUseCase(),
                getAllPaymentMethodsUseCase()
            ) { categories, paymentMethods ->
                categories to paymentMethods
            }.collectLatest { (categories, paymentMethods) ->
                when {
                    categories is ApiResult.Loading || paymentMethods is ApiResult.Loading -> {
                        _uiState.value = PurchaseNoteHomeFilterUiState(isLoading = true)
                    }

                    categories is ApiResult.Error || paymentMethods is ApiResult.Error -> {
                        _uiState.value = PurchaseNoteHomeFilterUiState(
                            isLoading = false,
                            errorMsg = "필터 목록을 불러오는데 실패하였습니다."
                        )
                    }

                    categories is ApiResult.Success && paymentMethods is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categoryItems = categories.data.toCategoryFilters(),
                                paymentMethodItem = paymentMethods.data.toPaymentMethodFilters()
                            )
                        }
                    }
                }
            }
        }
    }

    fun addOrDeleteCategoryFilter(categoryId: Int) {
        _uiState.update { uiState ->
            uiState.copy(
                categoryItems = uiState.categoryItems.map { category ->
                    if (category.categoryItem.id == categoryId) {
                        category.copy(isSelected = !category.isSelected)
                    } else {
                        category
                    }
                }
            )
        }
    }

    fun addOrDeletePaymentMethodFilter(paymentMethodId: Int) {
        _uiState.update { uiState ->
            uiState.copy(
                paymentMethodItem = uiState.paymentMethodItem.map { paymentMethod ->
                    if (paymentMethod.paymentMethod.paymentMethodId == paymentMethodId) {
                        paymentMethod.copy(isSelected = !paymentMethod.isSelected)
                    } else {
                        paymentMethod
                    }
                }
            )
        }
    }

    fun resetFilters() {
        _uiState.update { uiState ->
            uiState.copy(
                categoryItems = uiState.categoryItems.map {
                    it.copy(isSelected = false)
                },
                paymentMethodItem = uiState.paymentMethodItem.map {
                    it.copy(isSelected = false)
                }
            )
        }
    }

    fun setInitAppliedFilters(filters: List<Filters>) {
        _initAppliedFilters.value = filters
    }

    fun shownError() {
        _uiState.update {
            it.copy(errorMsg = null)
        }
    }

    private fun List<Category>.toCategoryFilters() =
        map { category ->
            Filters.Category(
                categoryItem = category.toUiModel(),
                isSelected = category.id in
                        initAppliedFilters.value.getAppliedCategoryIds().toSet()
            )
        }

    private fun List<PaymentMethod>.toPaymentMethodFilters() =
        map { paymentMethod ->
            Filters.PaymentMethod(
                paymentMethod = paymentMethod.toUiModel(),
                isSelected = paymentMethod.paymentMethodId in
                        initAppliedFilters.value.getAppliedPaymentMethodIds().toSet()
            )
        }
}

data class PurchaseNoteHomeFilterUiState(
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val categoryItems: List<Filters.Category> = emptyList(),
    val paymentMethodItem: List<Filters.PaymentMethod> = emptyList()
)

fun PurchaseNoteHomeFilterUiState.selectedFilterItems() =
    categoryItems.filter { it.isSelected } + paymentMethodItem.filter { it.isSelected }
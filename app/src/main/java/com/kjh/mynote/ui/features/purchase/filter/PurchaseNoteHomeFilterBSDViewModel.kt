package com.kjh.mynote.ui.features.purchase.filter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.PaymentMethod
import com.example.domain.usecase.ObserveAllCategoriesUseCase
import com.example.domain.usecase.ObserveAllPaymentMethodsUseCase
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
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase,
    private val observeAllPaymentMethodsUseCase: ObserveAllPaymentMethodsUseCase,
    val savedStateHandle: SavedStateHandle
): ViewModel() {

    var shouldScrollToEnd = false

    private val _initAppliedFilters = MutableStateFlow<List<Filters>>(emptyList())

    private val _uiState = MutableStateFlow(PurchaseNoteHomeFilterUiState())
    val uiState = _uiState.asStateFlow()

    val isChangedFilterFlow = combine(
        _initAppliedFilters, _uiState
    ) { initFilters, uiState ->
        initFilters != uiState.selectedFilters
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun getFilterItems() {
        viewModelScope.launch {
            combine(
                observeAllCategoriesUseCase(),
                observeAllPaymentMethodsUseCase()
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
                                paymentMethodItem = paymentMethods.data.toPaymentMethodFilters(),
                                selectedFilters = _initAppliedFilters.value
                            )
                        }
                    }
                }
            }
        }
    }

    fun addOrDeleteCategoryFilter(categoryFilter: Filters.Category) {
        _uiState.update { uiState ->
            val updateSelectedFilterItems = updateSelectedFilterItemsForCategory(categoryFilter)
            val currentSelectedFilterCount = uiState.selectedFilters.size

            shouldScrollToEnd = currentSelectedFilterCount < updateSelectedFilterItems.size

            uiState.copy(
                categoryItems = updateCategoryItems(categoryFilter),
                selectedFilters = updateSelectedFilterItems
            )
        }
    }

    fun addOrDeletePaymentMethodFilter(paymentMethodFilter: Filters.PaymentMethod) {
        _uiState.update { uiState ->
            val updateSelectedFilterItems = updateSelectedFilterItemsForPaymentMethod(paymentMethodFilter)
            val currentSelectedFilterCount = uiState.selectedFilters.size

            shouldScrollToEnd = currentSelectedFilterCount < updateSelectedFilterItems.size

            uiState.copy(
                paymentMethodItem = updatePaymentMethodItems(paymentMethodFilter),
                selectedFilters = updateSelectedFilterItems
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
                },
                selectedFilters = emptyList()
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

    private fun updateCategoryItems(categoryFilter: Filters.Category): List<Filters.Category> =
        _uiState.value.categoryItems.map { category ->
            if (category.categoryItem.id == categoryFilter.categoryItem.id) {
                category.copy(isSelected = !category.isSelected)
            } else {
                category
            }
        }

    private fun updatePaymentMethodItems(paymentMethodFilter: Filters.PaymentMethod): List<Filters.PaymentMethod> =
        _uiState.value.paymentMethodItem.map { paymentMethod ->
            if (paymentMethod.paymentMethod.paymentMethodId ==
                paymentMethodFilter.paymentMethod.paymentMethodId
            ) {
                paymentMethod.copy(isSelected = !paymentMethod.isSelected)
            } else {
                paymentMethod
            }
        }

    private fun updateSelectedFilterItemsForCategory(filter: Filters.Category): List<Filters> =
        _uiState.value.selectedFilters.toMutableList().apply {
            if (filter.isSelected) {
                removeAll {
                    it is Filters.Category && it.categoryItem.id == filter.categoryItem.id
                }
            } else {
                add(filter.copy(isSelected = true))
            }
        }

    private fun updateSelectedFilterItemsForPaymentMethod(filter: Filters.PaymentMethod): List<Filters> =
        _uiState.value.selectedFilters.toMutableList().apply {
            if (filter.isSelected) {
                removeAll {
                    it is Filters.PaymentMethod &&
                            it.paymentMethod.paymentMethodId == filter.paymentMethod.paymentMethodId
                }
            } else {
                add(filter.copy(isSelected = true))
            }
        }

    private fun List<Category>.toCategoryFilters() =
        map { category ->
            Filters.Category(
                categoryItem = category.toUiModel(),
                isSelected = category.id in
                        _initAppliedFilters.value.getAppliedCategoryIds().toSet()
            )
        }

    private fun List<PaymentMethod>.toPaymentMethodFilters() =
        map { paymentMethod ->
            Filters.PaymentMethod(
                paymentMethod = paymentMethod.toUiModel(),
                isSelected = paymentMethod.paymentMethodId in
                        _initAppliedFilters.value.getAppliedPaymentMethodIds().toSet()
            )
        }
}

data class PurchaseNoteHomeFilterUiState(
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val categoryItems: List<Filters.Category> = emptyList(),
    val paymentMethodItem: List<Filters.PaymentMethod> = emptyList(),
    val selectedFilters: List<Filters> = emptyList()
)
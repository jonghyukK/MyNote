package com.kjh.mynote.ui.features.category.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetCategoriesWithPurchaseNoteCountUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

data class CategoryListItem(
    val isSelected: Boolean = false,
    val isDefaultCategory: Boolean = false,
    val isEditable: Boolean = true,
    val categoryItem: CategoryUiModel
)

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoriesWithPurchaseNoteCountUseCase: GetCategoriesWithPurchaseNoteCountUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _isEditable = MutableStateFlow(savedStateHandle[CategoryListBSDialog.ARG_BOOL_IS_EDITABLE] ?: true)
    val isEditable = _isEditable.asSharedFlow()

    private val _selectedCategoryItem = MutableStateFlow<CategoryUiModel?>(savedStateHandle[AppConstants.INTENT_CATEGORY_ITEM])

    private val _categoriesWithPurchaseNoteCountFlow: StateFlow<List<CategoryUiModel>> = getCategoriesWithPurchaseNoteCountUseCase()
        .map { categories -> categories.map { categoryWithPurchaseNoteCount ->
            CategoryUiModel(
                id = categoryWithPurchaseNoteCount.categoryId,
                categoryName = categoryWithPurchaseNoteCount.categoryName,
                purchaseNoteCount = categoryWithPurchaseNoteCount.purchaseNoteCount
            )
        }}
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val uiState: StateFlow<List<CategoryListItem>> = combine(
        _categoriesWithPurchaseNoteCountFlow, _selectedCategoryItem
    ) { allCategories, selectedItem ->
        allCategories.map { category ->
            CategoryListItem(
                isSelected = category.id == selectedItem?.id,
                isDefaultCategory = category.id == 999,
                isEditable = _isEditable.value,
                categoryItem = category
            )
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}
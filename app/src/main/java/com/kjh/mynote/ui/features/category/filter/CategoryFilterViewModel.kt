package com.kjh.mynote.ui.features.category.filter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.features.category.filter.CategoryFilterDialogFragment.Companion.ARG_APPLIED_FILTER_ITEMS
import com.kjh.mynote.ui.features.category.list.CategoryListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 11..
 * Description:
 */

data class CategoryFilterUiState(
    val filterItems: List<CategoryListItem> = emptyList(),
    val isChanged: Boolean = false
)

@HiltViewModel
class CategoryFilterViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _appliedFilterItems: StateFlow<List<CategoryUiModel>> =
        savedStateHandle.getStateFlow(ARG_APPLIED_FILTER_ITEMS, emptyList())

    private val _selectedCategoryItems: MutableStateFlow<List<CategoryUiModel>> =
        MutableStateFlow(_appliedFilterItems.value)
    val selectedCategoryItems = _selectedCategoryItems.asStateFlow()

    private val _allCategoriesFlow = getAllCategoriesUseCase()
        .map { categories -> categories.map { it.toUiModel() } }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val uiState: StateFlow<CategoryFilterUiState> = combine(
        _allCategoriesFlow,
        _selectedCategoryItems
    ) { allCategories, selectedCategories ->
        CategoryFilterUiState(
            filterItems = allCategories.map { category ->
                CategoryListItem(
                    isSelected = selectedCategories.find { category.id == it.id } != null,
                    categoryItem = category
                )
            },
            isChanged = _appliedFilterItems.value.toSet() != _selectedCategoryItems.value.toSet()
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        CategoryFilterUiState()
    )

    fun addOrRemoveFilters(categoryItem: CategoryUiModel) {
        val currentSelectedItems = _selectedCategoryItems.value
        val filterOrAddItems = if (currentSelectedItems.contains(categoryItem)) {
            currentSelectedItems.filter { it != categoryItem }
        } else {
            currentSelectedItems + categoryItem
        }

        _selectedCategoryItems.value = filterOrAddItems
    }

    fun resetFilters() {
        _selectedCategoryItems.value = emptyList()
    }
}
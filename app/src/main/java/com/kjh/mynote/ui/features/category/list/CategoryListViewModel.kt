package com.kjh.mynote.ui.features.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Category
import com.example.domain.model.Result
import com.example.domain.model.onError
import com.example.domain.model.onLoading
import com.example.domain.model.onSuccess
import com.example.domain.usecase.DeleteCategoryAndAssignToETCUseCase
import com.example.domain.usecase.GetCategoriesWithPurchaseNoteCountUseCase
import com.example.domain.usecase.MakeCategoryUseCase
import com.example.domain.usecase.UpdateCategoryNameUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toDomainModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

data class CategoryListItem(
    val isSelected: Boolean = false,
    val isDefaultCategory: Boolean = false,
    val categoryItem: CategoryUiModel
)

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoriesWithPurchaseNoteCountUseCase: GetCategoriesWithPurchaseNoteCountUseCase,
    private val makeCategoryUseCase: MakeCategoryUseCase,
    private val updateCategoryNameUseCase: UpdateCategoryNameUseCase,
    private val deleteCategoryAndAssignToETCUseCase: DeleteCategoryAndAssignToETCUseCase
): ViewModel() {

    private val _selectedCategoryItem = MutableStateFlow<CategoryUiModel?>(null)

    private val _makeCategoryEventState = MutableSharedFlow<UiState<Unit>>()
    val makeCategoryEventState = _makeCategoryEventState.asSharedFlow()

    private val _updateCategoryNameEventState = MutableSharedFlow<UiState<CategoryUiModel>>()
    val updateCategoryNameEventState = _updateCategoryNameEventState.asSharedFlow()

    private val _deleteCategoryEventState = MutableSharedFlow<UiState<Int>>()
    val deleteCategoryEventState = _deleteCategoryEventState.asSharedFlow()

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
                categoryItem = category
            )
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun setSelectedCategoryItem(categoryItem: CategoryUiModel?) {
        _selectedCategoryItem.value = categoryItem
    }

    fun makeCategory(categoryName: String) {
        viewModelScope.launch {
            makeCategoryUseCase(Category(categoryName = categoryName)).collect { result ->
                result
                    .onLoading {
                        _makeCategoryEventState.emit(UiState.Loading)
                    }
                    .onError {
                        _makeCategoryEventState.emit(UiState.Error(it.message ?: "카테고리 추가가 실패하였습니다."))
                    }
                    .onSuccess {
                        _makeCategoryEventState.emit(UiState.Success(Unit))
                    }
            }
        }
    }

    fun editCategory(category: CategoryUiModel) {
        viewModelScope.launch {
            updateCategoryNameUseCase(category.toDomainModel()).collect { result ->
                result
                    .onLoading {
                        _updateCategoryNameEventState.emit(UiState.Loading)
                    }
                    .onError {
                        _updateCategoryNameEventState.emit(UiState.Error(it.message ?: "카테고리 수정이 실패하였습니다."))
                    }
                    .onSuccess {
                        _updateCategoryNameEventState.emit(UiState.Success(category))
                    }
            }
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            deleteCategoryAndAssignToETCUseCase(categoryId).collect { result ->
                result
                    .onLoading {
                        _deleteCategoryEventState.emit(UiState.Loading)
                    }
                    .onError {
                        _deleteCategoryEventState.emit(UiState.Error(it.message ?: "카테고리 삭제가 실패하였습니다."))
                    }
                    .onSuccess {
                        _deleteCategoryEventState.emit(UiState.Success(categoryId))
                    }
            }
        }
    }
}
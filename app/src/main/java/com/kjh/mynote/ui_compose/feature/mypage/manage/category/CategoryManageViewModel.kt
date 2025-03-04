package com.kjh.mynote.ui_compose.feature.mypage.manage.category

import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.usecase.DeleteCategoryByIdUseCase
import com.example.domain.usecase.MakeCategoryUseCase
import com.example.domain.usecase.ObserveAllCategoriesUseCase
import com.example.domain.usecase.UpdateCategoryNameUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui_compose.base.BaseComposeViewModel
import com.kjh.mynote.ui_compose.base.UiEvent
import com.kjh.mynote.ui_compose.base.UiSideEffect
import com.kjh.mynote.ui_compose.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 1..
 * Description:
 */

sealed class CategoryManageSideEffect: UiSideEffect {
    data class ShowErrorToast(val msg: String?): CategoryManageSideEffect()
}

sealed interface CategoryManageEvent: UiEvent {
    data object LoadingCategories: CategoryManageEvent
    data class LoadedCategories(val categories: List<CategoryUiModel>): CategoryManageEvent
    data class UpdateDialogState(val dialogState: DialogState): CategoryManageEvent
    data class AddCategory(val newCategoryName: String): CategoryManageEvent
    data class EditCategory(val category: CategoryUiModel): CategoryManageEvent
    data class DeleteCategory(val categoryId: Int): CategoryManageEvent
}

sealed class DialogState {
    data object Hidden: DialogState()
    data class Add(val categoryName: String): DialogState()
    data class Edit(val category: CategoryUiModel): DialogState()
    data class Delete(val category: CategoryUiModel): DialogState()
}

data class CategoryManageUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryUiModel> = emptyList(),
    val dialogState: DialogState = DialogState.Hidden
): UiState

@HiltViewModel
class CategoryManageComposeViewModel @Inject constructor(
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase,
    private val makeCategoryUseCase: MakeCategoryUseCase,
    private val updateCategoryNameUseCase: UpdateCategoryNameUseCase,
    private val deleteCategoryByIdUseCase: DeleteCategoryByIdUseCase,
): BaseComposeViewModel<CategoryManageEvent, CategoryManageUiState, CategoryManageSideEffect>() {

    override fun createInitialState(): CategoryManageUiState {
        return CategoryManageUiState()
    }

    init {
        fetchCategories()
    }

    override fun handleEvent(event: CategoryManageEvent) {
        when (event) {
            is CategoryManageEvent.AddCategory -> {
                if (isValidCategoryName(event.newCategoryName)) {
                    requestAddCategory(event.newCategoryName)
                }
            }
            is CategoryManageEvent.DeleteCategory -> {
                requestDeleteCategory(event.categoryId)
            }
            is CategoryManageEvent.EditCategory -> {
                if (isValidCategoryName(event.category.categoryName)) {
                    requestEditCategory(event.category)
                }
            }
            is CategoryManageEvent.LoadedCategories -> {
                setState { copy(isLoading = false, categories = event.categories) }
            }
            CategoryManageEvent.LoadingCategories -> {
                setState { copy(isLoading = true) }
            }
            is CategoryManageEvent.UpdateDialogState -> {
                setState { copy(dialogState = event.dialogState) }
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            observeAllCategoriesUseCase().collect { result ->
                when (result) {
                    ApiResult.Loading -> {
                        setEvent(CategoryManageEvent.LoadingCategories)
                    }
                    is ApiResult.Error -> {
                        setEvent(CategoryManageEvent.LoadedCategories(emptyList()))
                        setEffect { CategoryManageSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.LoadedCategories(result.data.toUiModel()))
                    }
                }
            }
        }
    }

    private fun isValidCategoryName(categoryName: String): Boolean {
        if (categoryName.isBlank()) {
            setEffect { CategoryManageSideEffect.ShowErrorToast("카테고리명을 입력해주세요.") }
            return false
        } else {
            return true
        }
    }

    private fun requestAddCategory(newCategoryName: String) {
        viewModelScope.launch {
            makeCategoryUseCase(Category(categoryName = newCategoryName)).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { CategoryManageSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.UpdateDialogState(DialogState.Hidden))
                    }
                }
            }
        }
    }

    private fun requestDeleteCategory(categoryId: Int) {
        viewModelScope.launch {
            deleteCategoryByIdUseCase(categoryId).collect { result ->
                when (result) {
                    ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { CategoryManageSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.UpdateDialogState(DialogState.Hidden))
                    }
                }
            }
        }
    }

    private fun requestEditCategory(category: CategoryUiModel) {
        viewModelScope.launch {
            updateCategoryNameUseCase(category.toDomainModel()).collect { result ->
                when (result) {
                    ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect { CategoryManageSideEffect.ShowErrorToast(result.error.message) }
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.UpdateDialogState(DialogState.Hidden))
                    }
                }
            }
        }
    }
}
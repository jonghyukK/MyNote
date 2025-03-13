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
import kotlinx.coroutines.delay
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
    data class UpdateDialogState(val dialogState: CategoryManageDialogState): CategoryManageEvent
    data class AddCategory(val newCategoryName: String): CategoryManageEvent
    data class EditCategory(val category: CategoryUiModel): CategoryManageEvent
    data class DeleteCategory(val categoryId: Int): CategoryManageEvent
}

sealed class CategoryManageDialogState {
    data object Hidden: CategoryManageDialogState()
    data class Add(val categoryName: String = ""): CategoryManageDialogState()
    data class Edit(val categoryItem: CategoryUiModel): CategoryManageDialogState()
    data class Delete(val categoryId: Int): CategoryManageDialogState()
}

data class CategoryManageUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryUiModel> = emptyList(),
    val manageDialogState: CategoryManageDialogState = CategoryManageDialogState.Hidden
): UiState

@HiltViewModel
class CategoryManageComposeViewModel @Inject constructor(
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase,
    private val makeCategoryUseCase: MakeCategoryUseCase,
    private val updateCategoryNameUseCase: UpdateCategoryNameUseCase,
    private val deleteCategoryByIdUseCase: DeleteCategoryByIdUseCase,
): BaseComposeViewModel<CategoryManageEvent, CategoryManageUiState, CategoryManageSideEffect>({CategoryManageUiState()}) {

    init {
        fetchCategories()
    }

    override fun reduceState(
        current: CategoryManageUiState,
        event: CategoryManageEvent,
    ): CategoryManageUiState {
        return when (event) {
            is CategoryManageEvent.LoadingCategories -> {
                current.copy(isLoading = true)
            }
            is CategoryManageEvent.LoadedCategories -> {
                current.copy(isLoading = false, categories = event.categories)
            }
            is CategoryManageEvent.UpdateDialogState -> {
                current.copy(manageDialogState = event.dialogState)
            }
            else -> {
                current
            }
        }
    }

    override fun handleEvent(event: CategoryManageEvent) {
        when (event) {
            is CategoryManageEvent.AddCategory -> {
                if (isValidCategoryName(event.newCategoryName)) {
                    requestAddCategory(event.newCategoryName)
                }
            }
            is CategoryManageEvent.EditCategory -> {
                if (isValidCategoryName(event.category.categoryName)) {
                    requestEditCategory(event.category)
                }
            }
            is CategoryManageEvent.DeleteCategory -> {
                requestDeleteCategory(event.categoryId)
            }
            else -> {
                setEvent(event)
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
                        setEffect(CategoryManageSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.LoadedCategories(result.data.toUiModel()))
                    }
                }
            }
        }
    }

    private fun requestAddCategory(newCategoryName: String) {
        viewModelScope.launch {
            makeCategoryUseCase(Category(categoryName = newCategoryName)).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect(CategoryManageSideEffect.ShowErrorToast(result.error.message))
                    }

                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.UpdateDialogState(CategoryManageDialogState.Hidden))
                    }
                }
            }
        }
    }

    private fun requestDeleteCategory(categoryId: Int) {
        viewModelScope.launch {
            deleteCategoryByIdUseCase(categoryId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect(CategoryManageSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.UpdateDialogState(CategoryManageDialogState.Hidden))
                    }
                }
            }
        }
    }

    private fun requestEditCategory(category: CategoryUiModel) {
        viewModelScope.launch {
            updateCategoryNameUseCase(category.toDomainModel()).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        setEffect(CategoryManageSideEffect.ShowErrorToast(result.error.message))
                    }
                    is ApiResult.Success -> {
                        setEvent(CategoryManageEvent.UpdateDialogState(CategoryManageDialogState.Hidden))
                    }
                }
            }
        }
    }

    private fun isValidCategoryName(categoryName: String): Boolean {
        if (categoryName.isBlank()) {
            setEffect(CategoryManageSideEffect.ShowErrorToast("카테고리명을 입력해주세요."))
            return false
        } else {
            return true
        }
    }
}
package com.kjh.mynote.ui.common.dialog.categorymanage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.usecase.DeleteCategoryByIdUseCase
import com.example.domain.usecase.MakeCategoryUseCase
import com.example.domain.usecase.UpdateCategoryNameUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toDomainModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@HiltViewModel
class CategoryAddOrDeleteOrEditDialogViewModel @Inject constructor(
    private val makeCategoryUseCase: MakeCategoryUseCase,
    private val updateCategoryNameUseCase: UpdateCategoryNameUseCase,
    private val deleteCategoryByIdUseCase: DeleteCategoryByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _makeCategoryEventState = MutableSharedFlow<CategoryManageEventState>()
    val makeCategoryEventState = _makeCategoryEventState.asSharedFlow()

    private val _updateCategoryNameEventState = MutableSharedFlow<CategoryManageEventState>()
    val updateCategoryNameEventState = _updateCategoryNameEventState.asSharedFlow()

    private val _deleteCategoryEventState = MutableSharedFlow<CategoryManageEventState>()
    val deleteCategoryEventState = _deleteCategoryEventState.asSharedFlow()

    fun makeCategory(categoryName: String) {
        viewModelScope.launch {
            makeCategoryUseCase(Category(categoryName = categoryName)).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _makeCategoryEventState.emit(CategoryManageEventState.Loading)
                    }
                    is ApiResult.Error -> {
                        _makeCategoryEventState.emit(CategoryManageEventState.Error(
                            result.error.message ?: "카테고리 추가가 실패하였습니다."))
                    }
                    is ApiResult.Success -> {
                        _makeCategoryEventState.emit(CategoryManageEventState.Success(result.data))
                    }
                }
            }
        }
    }

    fun editCategory(category: CategoryUiModel) {
        viewModelScope.launch {
            updateCategoryNameUseCase(category.toDomainModel()).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _updateCategoryNameEventState.emit(CategoryManageEventState.Loading)
                    }
                    is ApiResult.Error -> {
                        _updateCategoryNameEventState.emit(CategoryManageEventState.Error(
                            result.error.message ?: "카테고리 수정이 실패하였습니다."))
                    }
                    is ApiResult.Success -> {
                        _updateCategoryNameEventState.emit(CategoryManageEventState.Success(result.data))
                    }
                }
            }
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            deleteCategoryByIdUseCase(categoryId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _deleteCategoryEventState.emit(CategoryManageEventState.Loading)
                    }
                    is ApiResult.Error -> {
                        _deleteCategoryEventState.emit(CategoryManageEventState.Error(
                            result.error.message ?: "카테고리 삭제가 실패하였습니다."))
                    }
                    is ApiResult.Success -> {
                        _deleteCategoryEventState.emit(CategoryManageEventState.Success(categoryId.toLong()))
                    }
                }
            }
        }
    }
}

sealed interface CategoryManageEventState {
    data object Loading: CategoryManageEventState
    data class Error(val errorMsg: String): CategoryManageEventState
    data class Success(val categoryId: Long): CategoryManageEventState
}
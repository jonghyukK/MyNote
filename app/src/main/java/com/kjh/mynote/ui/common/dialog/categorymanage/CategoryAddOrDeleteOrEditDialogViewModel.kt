package com.kjh.mynote.ui.common.dialog.categorymanage

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.usecase.DeleteCategoryByIdUseCase
import com.example.domain.usecase.MakeCategoryUseCase
import com.example.domain.usecase.UpdateCategoryNameUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toDomainModel
import com.kjh.mynote.ui.common.dialog.categorymanage.CategoryAddOrDeleteOrEditDialog.Companion.ARG_CATEGORY_ITEM
import com.kjh.mynote.ui.common.dialog.categorymanage.CategoryAddOrDeleteOrEditDialog.Companion.ARG_ENUM_TYPE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@Parcelize
enum class CategoryDialogType: Parcelable {
    ADD,
    MODIFY,
    DELETE
}

@HiltViewModel
class CategoryAddOrDeleteOrEditDialogViewModel @Inject constructor(
    private val makeCategoryUseCase: MakeCategoryUseCase,
    private val updateCategoryNameUseCase: UpdateCategoryNameUseCase,
    private val deleteCategoryByIdUseCase: DeleteCategoryByIdUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val dialogType: StateFlow<CategoryDialogType> =
        savedStateHandle.getStateFlow(ARG_ENUM_TYPE, CategoryDialogType.ADD)

    val categoryItem: StateFlow<CategoryUiModel?> =
        savedStateHandle.getStateFlow(ARG_CATEGORY_ITEM, null)

    private val _makeCategoryEventState = MutableSharedFlow<UiState<Unit>>()
    val makeCategoryEventState = _makeCategoryEventState.asSharedFlow()

    private val _updateCategoryNameEventState = MutableSharedFlow<UiState<CategoryUiModel>>()
    val updateCategoryNameEventState = _updateCategoryNameEventState.asSharedFlow()

    private val _deleteCategoryEventState = MutableSharedFlow<UiState<Int>>()
    val deleteCategoryEventState = _deleteCategoryEventState.asSharedFlow()

    fun makeCategory(categoryName: String) {
        viewModelScope.launch {
            makeCategoryUseCase(Category(categoryName = categoryName)).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _makeCategoryEventState.emit(UiState.Loading)
                    }
                    is ApiResult.Error -> {
                        _makeCategoryEventState.emit(UiState.Error(result.error.message ?: "카테고리 추가가 실패하였습니다."))
                    }
                    is ApiResult.Success -> {
                        _makeCategoryEventState.emit(UiState.Success(Unit))
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
                        _updateCategoryNameEventState.emit(UiState.Loading)
                    }
                    is ApiResult.Error -> {
                        _updateCategoryNameEventState.emit(UiState.Error(result.error.message ?: "카테고리 수정이 실패하였습니다."))
                    }
                    is ApiResult.Success -> {
                        _updateCategoryNameEventState.emit(UiState.Success(category))
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
                        _deleteCategoryEventState.emit(UiState.Loading)
                    }
                    is ApiResult.Error -> {
                        _deleteCategoryEventState.emit(UiState.Error(result.error.message ?: "카테고리 수정이 실패하였습니다."))
                    }
                    is ApiResult.Success -> {
                        _deleteCategoryEventState.emit(UiState.Success(categoryId))
                    }
                }
            }
        }
    }
}
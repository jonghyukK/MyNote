package com.kjh.mynote.ui.features.mypage.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.asResult
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

sealed interface CategoryManageUiState {
    data object Loading: CategoryManageUiState
    data class Error(val error: Throwable): CategoryManageUiState
    data class Categories(val categoryItems: List<CategoryUiModel>): CategoryManageUiState
}

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
): ViewModel() {

    val uiState = getAllCategoriesUseCase()
        .asResult()
        .map { result ->
            when (result) {
                is ApiResult.Loading -> CategoryManageUiState.Loading
                is ApiResult.Error -> CategoryManageUiState.Error(result.error)
                is ApiResult.Success -> CategoryManageUiState.Categories(result.data.toUiModel())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryManageUiState.Loading
        )
}
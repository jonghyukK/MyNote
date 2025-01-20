package com.kjh.mynote.ui.features.mypage.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.usecase.GetAllCategoriesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
): ViewModel() {

    private val _shownError = MutableStateFlow(false)

    val uiState: StateFlow<CategoryManageUiState> = combine(
        _shownError, getAllCategoriesUseCase()
    ) { shownError, categoryResult ->
        when (categoryResult) {
            is ApiResult.Loading -> {
                CategoryManageUiState.Loading
            }
            is ApiResult.Error -> {
                CategoryManageUiState.Error(if (shownError) null else categoryResult.error.message)
            }
            is ApiResult.Success -> {
                CategoryManageUiState.Success(categoryResult.data.toUiModel())
            }
        }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CategoryManageUiState.Loading
        )

    fun shownError() {
        _shownError.value = true
    }
}

sealed interface CategoryManageUiState {
    data object Loading: CategoryManageUiState
    data class Error(val errorMsg: String?): CategoryManageUiState
    data class Success(val categoryItems: List<CategoryUiModel>): CategoryManageUiState
}
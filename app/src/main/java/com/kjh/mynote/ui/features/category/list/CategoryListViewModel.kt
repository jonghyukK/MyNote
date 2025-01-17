package com.kjh.mynote.ui.features.category.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.usecase.GetCategoriesWithNoteCountsUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_BOOL_IS_EDITABLE
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_OBJ_SELECTED_CATEGORY_ITEM
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_STR_DATE
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoriesWithNoteCountsUseCase: GetCategoriesWithNoteCountsUseCase,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val queryDate: LocalDate? =
        savedStateHandle.get<String>(ARG_STR_DATE)?.let { LocalDate.parse(it) }

    private val isEditable: Boolean =
        savedStateHandle[ARG_BOOL_IS_EDITABLE] ?: true

    private val selectedCategoryItem: CategoryUiModel? =
        savedStateHandle[ARG_OBJ_SELECTED_CATEGORY_ITEM]

    val uiState: StateFlow<CategoryListUiState> =
        getCategoriesWithNoteCountsUseCase(
            queryDate?.getFirstDayOfMonth()?.toMillis(),
            queryDate?.getLastDayOfMonth()?.toMillis()
        )
            .map { result -> handleResult(result) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                CategoryListUiState.Loading
            )

    private fun handleResult(result: ApiResult<List<CategoryWithPurchaseNoteCount>>): CategoryListUiState {
        return when (result) {
            is ApiResult.Loading -> {
                CategoryListUiState.Loading
            }
            is ApiResult.Error -> {
                CategoryListUiState.Error(result.error.message ?: "카테고리 목록을 불러오는데 실패하였습니다.")
            }
            is ApiResult.Success -> {
                CategoryListUiState.Success(
                    categoryItems = result.data.map { category ->
                        CategoryListItem(
                            isSelected = category.categoryId == selectedCategoryItem?.id,
                            isEditable = isEditable,
                            categoryItem = category.toUiModel()
                        )
                    }
                )
            }
        }
    }
}

data class CategoryListItem(
    val isSelected: Boolean = false,
    val isEditable: Boolean = true,
    val categoryItem: CategoryUiModel
)

sealed interface CategoryListUiState {
    data object Loading: CategoryListUiState
    data class Error(val errorMsg: String): CategoryListUiState
    data class Success(val categoryItems: List<CategoryListItem>): CategoryListUiState
}
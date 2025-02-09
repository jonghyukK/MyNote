package com.kjh.mynote.ui.features.category.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.usecase.GetCategoriesWithNoteCountsUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_BOOL_IS_EDITABLE
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_BOOL_SHOW_COUNT
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_OBJ_SELECTED_CATEGORY_ITEM
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog.Companion.ARG_STR_DATE
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
): BaseViewModel() {

    private val queryDate: LocalDate? =
        savedStateHandle.get<String>(ARG_STR_DATE)?.let { LocalDate.parse(it) }

    private val isEditable: Boolean =
        savedStateHandle[ARG_BOOL_IS_EDITABLE] ?: true

    private val showCount: Boolean =
        savedStateHandle[ARG_BOOL_SHOW_COUNT] ?: false

    private val selectedCategoryItem = MutableStateFlow<CategoryUiModel?>(
        savedStateHandle[ARG_OBJ_SELECTED_CATEGORY_ITEM])

    private val _updateSelectedItemEvent = Channel<CategoryUiModel?>()
    val updateSelectedItemEvent = _updateSelectedItemEvent.receiveAsFlow()

    val categoryItems: StateFlow<List<CategoryListItem>> =
        getCategoriesWithNoteCountsUseCase(
            queryDate?.getFirstDayOfMonth()?.toMillis(),
            queryDate?.getLastDayOfMonth()?.toMillis()
        )
            .onEach { result ->
                if (result is ApiResult.Success) {
                    updateSelectedCategoryItem(result.data.map { it.toUiModel() })
                }
            }
            .map(::mapResultToList)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private fun mapResultToList(
        result: ApiResult<List<CategoryWithPurchaseNoteCount>>,
    ): List<CategoryListItem> = when (result) {
        is ApiResult.Loading -> emptyList()

        is ApiResult.Error -> {
            sendError(result.error.message)
            emptyList()
        }

        is ApiResult.Success -> {
            result.data.map { it.toUiModel() }
                .map { category ->
                    CategoryListItem(
                        isSelected = category.id == selectedCategoryItem.value?.id,
                        isEditable = isEditable,
                        showCount = showCount,
                        categoryItem = category
                    )
                }
        }
    }

    private suspend fun updateSelectedCategoryItem(categoryItems: List<CategoryUiModel>) {
        val matchedIdItem = categoryItems.find {
            it.id == selectedCategoryItem.value?.id
        }

        if (matchedIdItem != selectedCategoryItem.value) {
            _updateSelectedItemEvent.send(matchedIdItem)
            selectedCategoryItem.value = matchedIdItem
        }
    }
}

data class CategoryListItem(
    val isSelected: Boolean = false,
    val isEditable: Boolean = true,
    val showCount: Boolean = false,
    val categoryItem: CategoryUiModel
)
package com.kjh.mynote.ui.features.mypage.category

import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.ObserveAllCategoriesUseCase
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val observeAllCategoriesUseCase: ObserveAllCategoriesUseCase
): BaseViewModel() {

    val uiState: StateFlow<UiState<List<CategoryUiModel>>> =
        observeAllCategoriesUseCase()
            .mapResultToUiState { it.toUiModel() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState.Loading
            )
}
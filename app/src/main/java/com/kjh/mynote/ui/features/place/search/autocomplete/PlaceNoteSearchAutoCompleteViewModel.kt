package com.kjh.mynote.ui.features.place.search.autocomplete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ApiResult
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.usecase.SearchPlaceNotesWithCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */

@HiltViewModel
class PlaceNoteSearchAutoCompleteViewModel @Inject constructor(
    private val searchPlaceNotesWithCountUseCase: SearchPlaceNotesWithCountUseCase
): ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val searchAutoCompleteUiState = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                return@flatMapLatest flowOf(SearchAutoCompleteUiState.Init)
            }

            searchPlaceNotesWithCountUseCase(query).map { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        SearchAutoCompleteUiState.Loading
                    }
                    is ApiResult.Error -> {
                        SearchAutoCompleteUiState.Error(result.error.message)
                    }
                    is ApiResult.Success -> {
                        val items = result.data
                        if (items.isEmpty()) {
                            SearchAutoCompleteUiState.Empty
                        } else {
                            val resultItems = items.map { item ->
                                PlaceNoteSearchAutoCompleteItem(
                                    queryText = query,
                                    item = item,
                                    count = item.count
                                )
                            }

                            SearchAutoCompleteUiState.Success(resultItems)
                        }
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchAutoCompleteUiState.Init
        )

    fun setSearchQuery(text: String) {
        _searchQuery.value = text
    }
}

data class PlaceNoteSearchAutoCompleteItem(
    val queryText: String,
    val item: SearchPlaceNoteWithCount,
    val count: Int
)

sealed interface SearchAutoCompleteUiState {
    data object Init: SearchAutoCompleteUiState
    data object Loading: SearchAutoCompleteUiState
    data object Empty: SearchAutoCompleteUiState
    data class Error(val msg: String?): SearchAutoCompleteUiState
    data class Success(val data: List<PlaceNoteSearchAutoCompleteItem>): SearchAutoCompleteUiState
}
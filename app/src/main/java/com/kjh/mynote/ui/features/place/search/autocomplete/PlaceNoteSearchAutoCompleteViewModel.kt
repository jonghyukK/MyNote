package com.kjh.mynote.ui.features.place.search.autocomplete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.usecase.SearchPlaceNotesWithCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    private val _uiState = MutableStateFlow<SearchAutoCompleteUiState>(SearchAutoCompleteUiState.Init)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isNotBlank()) {
                        searchPlaceNotesWithCountUseCase(query).map { result ->
                            when (result) {
                                is Result.Loading -> {
                                    SearchAutoCompleteUiState.Loading
                                }
                                is Result.Error -> {
                                    SearchAutoCompleteUiState.Error(result.msg)
                                }
                                is Result.Success -> {
                                    val items = result.data ?: emptyList()
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
                    } else {
                        flowOf(SearchAutoCompleteUiState.Init)
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun setSearchQuery(text: String) {
        if (text.isBlank()) {
            _uiState.value = SearchAutoCompleteUiState.Init
        }

        _searchQuery.value = text
    }

    fun shownErrorMessage() {
        _uiState.value = SearchAutoCompleteUiState.Init
    }
}

data class PlaceNoteSearchAutoCompleteItem(
    val queryText: String,
    val item: SearchPlaceNoteWithCount,
    val count: Int
)

sealed class SearchAutoCompleteUiState {
    data object Init: SearchAutoCompleteUiState()
    data object Loading: SearchAutoCompleteUiState()
    data object Empty: SearchAutoCompleteUiState()
    data class Error(val msg: String?): SearchAutoCompleteUiState()
    data class Success(val data: List<PlaceNoteSearchAutoCompleteItem>): SearchAutoCompleteUiState()
}
package com.kjh.mynote.ui.features.place.search

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

data class SearchResultItem(
    val queryText: String,
    val item: SearchPlaceNoteWithCount,
    val count: Int
)

@HiltViewModel
class SearchPlaceNoteViewModel @Inject constructor(
    private val searchPlaceNotesWithCountUseCase: SearchPlaceNotesWithCountUseCase
): ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Init)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isNotEmpty()) {
                        searchPlaceNotesWithCountUseCase(query).map { result ->
                            when (result) {
                                is Result.Loading -> {
                                    SearchUiState.Loading
                                }
                                is Result.Error -> {
                                    SearchUiState.Error(result.msg)
                                }
                                is Result.Success -> {
                                    val items = result.data ?: emptyList()
                                    if (items.isEmpty()) {
                                        SearchUiState.Empty
                                    } else {
                                        val resultItems = items.map { item ->
                                            SearchResultItem(
                                                queryText = query,
                                                item = item,
                                                count = item.count
                                            )
                                        }

                                        SearchUiState.Success(resultItems)
                                    }
                                }
                            }
                        }
                    } else {
                        flowOf(SearchUiState.Init)
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun setSearchQuery(text: String) {
        _searchQuery.value = text
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState.Init
    }
}

sealed class SearchUiState {
    data object Init: SearchUiState()
    data object Loading: SearchUiState()
    data object Empty: SearchUiState()
    data class Error(val msg: String?): SearchUiState()
    data class Success(val data: List<SearchResultItem>): SearchUiState()
}
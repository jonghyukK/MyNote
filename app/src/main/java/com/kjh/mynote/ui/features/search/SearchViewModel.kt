package com.kjh.mynote.ui.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.PlaceNoteWithSamePlaceNameNotes
import com.example.domain.model.Result
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.usecase.SearchPlaceNotesWithCountUseCase
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class SearchResultItem(
    val queryText: String,
    val item: SearchPlaceNoteWithCount,
    val count: Int
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchPlaceNotesWithCountUseCase: SearchPlaceNotesWithCountUseCase
): ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<SearchUiState> = _searchQuery
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

                                SearchUiState.Results(resultItems)
                            }
                        }
                    }
                }
            } else {
                flowOf(SearchUiState.Empty)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchUiState.Empty
        )

    fun setSearchQuery(text: String) {
        _searchQuery.value = text
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }
}

sealed class SearchUiState {
    data object Loading: SearchUiState()
    data object Empty: SearchUiState()
    data class Error(val msg: String?): SearchUiState()
    data class Results(val data: List<SearchResultItem>): SearchUiState()
}
package com.kjh.mynote.ui.features.place.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.usecase.SearchPlaceNotesWithCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 20..
 * Description:
 */

@HiltViewModel
class PlaceNoteSearchViewModel @Inject constructor(): ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _lastActionQuery = MutableStateFlow<String?>(null)

    val isQueryChangedAfterSearchAction = combine(
        _searchQuery, _lastActionQuery
    ) { searchQuery, actionQuery ->
        actionQuery != null && searchQuery != actionQuery
    }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            false
        )

    fun setSearchQuery(text: String) {
        _searchQuery.value = text
    }

    fun saveActionQuery() {
        _lastActionQuery.value = _searchQuery.value
    }

    fun clearActionQuery() {
        _lastActionQuery.value = null
    }
}
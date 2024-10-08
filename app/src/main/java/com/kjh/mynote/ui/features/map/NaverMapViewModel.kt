package com.kjh.mynote.ui.features.map

import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.data.model.Result
import com.kjh.data.repository.KakaoMapRepository
import com.kjh.mynote.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

sealed class NaverMapSearchState {
    object Searching: NaverMapSearchState()
    object Nothing: NaverMapSearchState()
    object Founded: NaverMapSearchState()
}

@HiltViewModel
class NaverMapViewModel @Inject constructor(
    private val kakaoMapRepository: KakaoMapRepository
): BaseViewModel() {

    private val _placeItems = MutableStateFlow<List<KakaoPlaceModel>>(emptyList())
    val placeItems = _placeItems.asStateFlow()

    private val _naverMapSearchEvent = MutableSharedFlow<NaverMapSearchState>()
    val naverMapSearchEvent = _naverMapSearchEvent.asSharedFlow()

    private val _selectPlaceItemEvent = MutableSharedFlow<KakaoPlaceModel>()
    val selectPlaceItemEvent = _selectPlaceItemEvent.asSharedFlow()

    fun getPlacesByQuery(query: String) {
        viewModelScope.launch {
            kakaoMapRepository.getPlacesByQuery(query)
                .collect { apiResult ->
                    when (apiResult) {
                        Result.Loading -> {
                            _naverMapSearchEvent.emit(NaverMapSearchState.Searching)
                        }
                        is Result.Success -> {
                            delay(700)

                            val placeList = apiResult.data?.documents ?: emptyList()
                            _placeItems.value = placeList

                            if (placeList.isEmpty()) {
                                _naverMapSearchEvent.emit(NaverMapSearchState.Nothing)
                                return@collect
                            }

                            _naverMapSearchEvent.emit(NaverMapSearchState.Founded)
                            _selectPlaceItemEvent.emit(placeList[0])
                        }
                        is Result.Error -> {
                            emitError(apiResult.msg)
                        }
                    }
                }
        }
    }

    fun setSelectPlace(placeItem: KakaoPlaceModel) {
        viewModelScope.launch {
            _selectPlaceItemEvent.emit(placeItem)
        }
    }

    fun isEmptyPlaceItems() = _placeItems.value.isEmpty()
}
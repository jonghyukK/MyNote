package com.kjh.mynote.ui.features.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Result
import com.example.domain.usecase.GetKakaoPlacesByQueryUseCase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.ui.base.BaseViewModel
import com.kjh.mynote.utils.constants.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

data class NaverMapUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val placeItems: List<PlaceInfoUiModel> = emptyList(),
    val sheetBehavior: Int = BottomSheetBehavior.STATE_HIDDEN,
    val movingCameraPlaceItem: PlaceInfoUiModel? = null
)

@HiltViewModel
class NaverMapViewModel @Inject constructor(
    private val getKakaoPlacesByQueryUseCase: GetKakaoPlacesByQueryUseCase,
    private val savedStateHandle: SavedStateHandle
): BaseViewModel() {

    private val prevAttachedPlaceItem =
        savedStateHandle.get<PlaceInfoUiModel>(AppConstants.INTENT_TEMP_PLACE_ITEM)

    private val _uiState = MutableStateFlow(NaverMapUiState())
    val uiState = _uiState.asStateFlow()

    val showOpenButtonFlow = _uiState.map {
        it.placeItems.isNotEmpty()
                && it.sheetBehavior == BottomSheetBehavior.STATE_HIDDEN
                && it.movingCameraPlaceItem == null
    }.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed()
    )

    fun checkInitData() {
        if (prevAttachedPlaceItem != null) {
            _uiState.value = NaverMapUiState(
                placeItems = listOf(prevAttachedPlaceItem),
                sheetBehavior = BottomSheetBehavior.STATE_COLLAPSED,
                movingCameraPlaceItem = prevAttachedPlaceItem
            )
        }
    }

    fun getPlacesByQuery(query: String) {
        viewModelScope.launch {
            getKakaoPlacesByQueryUseCase(query)
                .collect { apiResult ->
                    when (apiResult) {
                        is Result.Loading -> {
                            _uiState.value = NaverMapUiState(isLoading = true)
                        }
                        is Result.Success -> {
                            delay(500)

                            val placeList = apiResult.data?.toUiModel() ?: emptyList()
                            if (placeList.isNotEmpty()) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        placeItems = placeList,
                                        movingCameraPlaceItem = placeList[0]
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isEmpty = true,
                                        placeItems = placeList
                                    )
                                }
                            }
                        }
                        is Result.Error -> {
                            emitError(apiResult.msg)
                        }
                    }
                }
        }
    }

    fun shownEmptyToast() {
        _uiState.update {
            it.copy(isEmpty = false)
        }
    }

    fun setSheetBehaviorTo(state: Int) {
        _uiState.update {
            it.copy(sheetBehavior = state)
        }
    }

    fun setMovingCameraPlaceItem(placeItem: PlaceInfoUiModel) {
        _uiState.update {
            it.copy(movingCameraPlaceItem = placeItem)
        }
    }

    fun clearMovingCameraPlaceItemWithStateCollapsed() {
        _uiState.update {
            it.copy(
                movingCameraPlaceItem = null,
                sheetBehavior = BottomSheetBehavior.STATE_COLLAPSED
            )
        }
    }
}
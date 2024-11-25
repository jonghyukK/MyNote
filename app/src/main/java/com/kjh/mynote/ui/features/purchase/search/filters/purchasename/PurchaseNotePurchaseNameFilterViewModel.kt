package com.kjh.mynote.ui.features.purchase.search.filters.purchasename

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 26..
 * Description:
 */

data class PurchaseNotePurchaseNameFilterUiState(
    val initPurchaseName: String = "",
    val tempPurchaseName: String = initPurchaseName
)

fun PurchaseNotePurchaseNameFilterUiState.isChanged() =
    initPurchaseName != tempPurchaseName

@HiltViewModel
class PurchaseNotePurchaseNameFilterViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseNotePurchaseNameFilterUiState())
    val uiState = _uiState.asStateFlow()

    fun setInitPurchaseName(name: String) {
        _uiState.value = PurchaseNotePurchaseNameFilterUiState(initPurchaseName = name)
    }

    fun setTempPurchaseName(name: String) {
        _uiState.update {
            it.copy(tempPurchaseName = name)
        }
    }

    fun clearPurchaseName() {
        _uiState.update {
            it.copy(tempPurchaseName = "")
        }
    }
}
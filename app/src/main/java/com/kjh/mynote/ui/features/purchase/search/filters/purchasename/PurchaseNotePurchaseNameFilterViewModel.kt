package com.kjh.mynote.ui.features.purchase.search.filters.purchasename

import androidx.lifecycle.ViewModel
import com.kjh.mynote.ui.features.purchase.search.Filters
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
    val initPurchaseNameFilter: Filters.PurchaseName = Filters.PurchaseName(),
    val tempPurchaseNameFilter: Filters.PurchaseName = initPurchaseNameFilter
)

fun PurchaseNotePurchaseNameFilterUiState.isChanged() =
    initPurchaseNameFilter.purchaseName != tempPurchaseNameFilter.purchaseName

@HiltViewModel
class PurchaseNotePurchaseNameFilterViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseNotePurchaseNameFilterUiState())
    val uiState = _uiState.asStateFlow()

    fun setInitPurchaseNameFilter(name: String) {
        _uiState.value = PurchaseNotePurchaseNameFilterUiState(
            initPurchaseNameFilter = Filters.PurchaseName(name)
        )
    }

    fun setTempPurchaseNameFilter(name: String) {
        _uiState.update {
            it.copy(
                tempPurchaseNameFilter = it.tempPurchaseNameFilter.copy(
                    purchaseName = name,
                    isApplied = name.isNotBlank()
                )
            )
        }
    }

    fun clearTempFilter() {
        _uiState.update {
            it.copy(
                tempPurchaseNameFilter = Filters.PurchaseName()
            )
        }
    }
}
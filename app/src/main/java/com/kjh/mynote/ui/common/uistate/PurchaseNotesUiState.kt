package com.kjh.mynote.ui.common.uistate

import com.kjh.mynote.model.PurchaseNoteUiModel
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:ø
 */
sealed class PurchaseNotesUiState {

    data class DateItem(val date: LocalDate): PurchaseNotesUiState()

    data class PurchaseNoteItem(val purchaseNote: PurchaseNoteUiModel): PurchaseNotesUiState()
}
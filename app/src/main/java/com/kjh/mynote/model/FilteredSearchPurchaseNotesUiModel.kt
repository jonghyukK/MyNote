package com.kjh.mynote.model

import com.example.domain.model.FilteredSearchPurchaseNotes
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */
data class FilteredSearchPurchaseNotesUiModel(
    val date: LocalDate,
    val purchaseNoteItems: List<PurchaseNoteUiModel>
)

/**
 *  FilteredSearchPlaceNotes (domain) -> FilteredSearchPlaceNotesUiModel (presentation)
 */
fun FilteredSearchPurchaseNotes.toUiModel() =
    FilteredSearchPurchaseNotesUiModel(
        date = date,
        purchaseNoteItems = purchaseNotes.toUiModel()
    )

fun List<FilteredSearchPurchaseNotes>.toUiModel() = map(FilteredSearchPurchaseNotes::toUiModel)
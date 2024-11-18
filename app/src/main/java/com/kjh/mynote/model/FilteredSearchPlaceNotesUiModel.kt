package com.kjh.mynote.model

import com.example.domain.model.FilteredSearchPlaceNotes
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */
data class FilteredSearchPlaceNotesUiModel(
    val date: LocalDate,
    val placeNoteItems: List<PlaceNoteUiModel>
)

/**
 *  FilteredSearchPlaceNotes (domain) -> FilteredSearchPlaceNotesUiModel (presentation)
 */
fun FilteredSearchPlaceNotes.toUiModel() =
    FilteredSearchPlaceNotesUiModel(
        date = date,
        placeNoteItems = placeNotes.toUiModel()
    )

fun List<FilteredSearchPlaceNotes>.toUiModel() = map(FilteredSearchPlaceNotes::toUiModel)
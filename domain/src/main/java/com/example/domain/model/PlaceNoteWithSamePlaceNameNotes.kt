package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
data class PlaceNoteWithSamePlaceNameNotes(
    val placeNote: PlaceNote,
    val samePlaceNameNotes: List<PlaceNote>
)
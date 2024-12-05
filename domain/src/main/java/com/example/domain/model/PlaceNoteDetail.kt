package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 5..
 * Description:
 */
data class PlaceNoteDetail(
    val placeNote: PlaceNote,
    val samePlaceNameNotes: List<PlaceNote>,
    val purchaseNotes: List<PurchaseNote>
)
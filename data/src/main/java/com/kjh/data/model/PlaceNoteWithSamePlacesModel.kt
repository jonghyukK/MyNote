package com.kjh.data.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 24..
 * Description:
 */
data class PlaceNoteWithSamePlacesModel(
    val placeNoteModel: PlaceNoteModel,
    val samePlaceNameNoteModels: List<PlaceNoteModel>
)
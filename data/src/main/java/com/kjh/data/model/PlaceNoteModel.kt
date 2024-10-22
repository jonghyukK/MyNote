package com.kjh.data.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 2..
 * Description:
 */
data class PlaceNoteModel(
    val id: Int = 0,
    val placeImages: List<String>,
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String? = null,
    val x: String,
    val y: String,
    val visitDate: Long,
    val noteTitle: String,
    val noteContents: String,
) {
   val placeRegion = placeAddress.split(" ").run {
        "${this[0]}, ${this[1]}"
    }
}
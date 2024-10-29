package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 28..
 * Description:
 */

data class PlaceNote(
    val id: Int = 0,
    val placeImages: List<String>,
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String? = null,
    val x: String,
    val y: String,
    val visitDate: Long,
    val noteTitle: String,
    val noteContents: String
)
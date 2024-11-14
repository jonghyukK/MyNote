package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 28..
 * Description:
 */

data class PlaceNote(
    val id: Int = 0,
    val placeImages: List<String>,
    val placeInfo: PlaceInfo,
    val visitDate: Long,
    val noteContents: String
)
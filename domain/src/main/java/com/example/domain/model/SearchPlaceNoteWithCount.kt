package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

data class SearchPlaceNoteWithCount(
    val id: Int,
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String? = null,
    val count: Int
)

package com.example.data.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 2..
 * Description:
 */
data class PlaceNoteModel(
    val id: Int = 0,
    val placeImages: List<String>,
    val description: String,
    val placeName: String,
    val placeAddress: String,
    val x: Long,
    val y: Long,
    val startDate: Long,
    val endDate: Long
)
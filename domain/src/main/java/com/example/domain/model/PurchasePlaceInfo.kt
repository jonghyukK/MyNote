package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

data class PurchasePlaceInfo(
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String? = null,
    val x: String,
    val y: String
)
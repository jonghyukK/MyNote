package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */

data class KakaoPlace(
    val id: String,
    val placeName: String,
    val addressName: String,
    val roadAddressName: String,
    val x: String,
    val y: String
)
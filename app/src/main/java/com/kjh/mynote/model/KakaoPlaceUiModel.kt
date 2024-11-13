package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.KakaoPlace
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */

@Parcelize
data class KakaoPlaceUiModel(
    val id: String = "",
    val placeName: String,
    val addressName: String,
    val roadAddressName: String? = null,
    val x: String,
    val y: String
): Parcelable

fun KakaoPlace.toUiModel() = KakaoPlaceUiModel(
    id = id,
    placeName = placeName,
    addressName = addressName,
    roadAddressName = roadAddressName,
    x = x,
    y = y
)

fun List<KakaoPlace>.toUiModel() = map(KakaoPlace::toUiModel)
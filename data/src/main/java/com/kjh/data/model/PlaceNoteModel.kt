package com.kjh.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 2..
 * Description:
 */

@Parcelize
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
): Parcelable {
   val placeRegion = placeAddress.split(" ").run {
        "${this[0]}, ${this[1]}"
    }
}

fun PlaceNoteModel.mapToKakaoPlaceModel() = KakaoPlaceModel(
    id = id.toString(),
    placeName = placeName,
    addressName = placeAddress,
    roadAddressName = placeRoadAddress ?: "",
    x = x,
    y = y
)
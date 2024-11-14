package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PlaceInfo
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */

@Parcelize
data class PlaceInfoUiModel(
    val id: String = "",
    val placeName: String,
    val address: String,
    val roadAddress: String,
    val x: String,
    val y: String,
): Parcelable {
    val placeRegion: String
        get() = address.split(" ").run {
        "${this[0]}, ${this[1]}"
    }
}

/**
 *  PlaceInfo (domain) -> PlaceInfoUiModel (presentation)
 */
fun PlaceInfo.toUiModel() = PlaceInfoUiModel(
    id = id,
    placeName = name,
    address = address,
    roadAddress = roadAddress,
    x = x,
    y = y
)

fun List<PlaceInfo>.toUiModel() = map(PlaceInfo::toUiModel)

/**
 *  PlaceInfoUiModel (presentation) -> PlaceInfo (domain)
 */
fun PlaceInfoUiModel.toDomainModel() = PlaceInfo(
    id = id,
    name = placeName,
    address = address,
    roadAddress = roadAddress,
    x = x,
    y = y
)
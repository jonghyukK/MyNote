package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PlaceNote
import com.kjh.mynote.utils.extensions.toLocalDate
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 28..
 * Description:
 */

@Parcelize
data class PlaceNoteUiModel(
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
    val placeRegion: String,
    val localDate: LocalDate
): Parcelable

fun PlaceNote.toUiModel() = PlaceNoteUiModel(
    id = id,
    placeImages = placeImages,
    placeName = placeName,
    placeAddress = placeAddress,
    placeRoadAddress = placeRoadAddress,
    x = x,
    y = y,
    visitDate = visitDate,
    noteTitle = noteTitle,
    noteContents = noteContents,
    placeRegion = placeAddress.split(" ").run {
        "${this[0]}, ${this[1]}"
    },
    localDate = visitDate.toLocalDate()
)

fun List<PlaceNote>.toUiModel() = map(PlaceNote::toUiModel)

fun PlaceNoteUiModel.toDomainModel() = PlaceNote(
    id = id,
    placeImages = placeImages,
    placeName = placeName,
    placeAddress = placeAddress,
    placeRoadAddress = placeRoadAddress,
    x = x,
    y = y,
    visitDate = visitDate,
    noteTitle = noteTitle,
    noteContents = noteContents,
)

fun PlaceNoteUiModel.toKakaoPlaceUiModel() = KakaoPlaceUiModel(
    id = id.toString(),
    placeName = placeName,
    addressName = placeAddress,
    roadAddressName = placeRoadAddress,
    x = x,
    y = y
)
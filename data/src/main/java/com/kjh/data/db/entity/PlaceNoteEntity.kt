package com.kjh.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kjh.data.model.PlaceNoteModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 2..
 * Description:
 */

@Entity(tableName = "places")
data class PlaceNoteEntity(
    @PrimaryKey(autoGenerate = true)
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
)

fun PlaceNoteEntity.toExternal() = PlaceNoteModel(
    id = id,
    placeImages = placeImages,
    placeName = placeName,
    placeAddress = placeAddress,
    placeRoadAddress = placeRoadAddress,
    x = x,
    y = y,
    visitDate = visitDate,
    noteTitle = noteTitle,
    noteContents = noteContents
)

fun List<PlaceNoteEntity>.toExternal() = map(PlaceNoteEntity::toExternal)

fun PlaceNoteModel.toEntity() = PlaceNoteEntity(
    placeImages = placeImages,
    placeName = placeName,
    placeAddress = placeAddress,
    placeRoadAddress = placeRoadAddress,
    x = x,
    y = y,
    visitDate = visitDate,
    noteTitle = noteTitle,
    noteContents = noteContents
)
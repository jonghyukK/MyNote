package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.PlaceNote

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

fun PlaceNoteEntity.toDomainModel() = PlaceNote(
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

fun List<PlaceNoteEntity>.toDomainModel() = map(PlaceNoteEntity::toDomainModel)

fun PlaceNote.toEntity() = PlaceNoteEntity(
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
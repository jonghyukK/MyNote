package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.PlaceInfo
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
    val placeId: String,
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String,
    val x: String,
    val y: String,
    val visitDate: Long,
    val noteTitle: String,
    val noteContents: String,
)

/**
 *  PlaceNoteEntity(Data) -> PlaceNote(Domain)
 */
fun PlaceNoteEntity.toDomainModel() = PlaceNote(
    id = id,
    placeImages = placeImages,
    placeInfo = PlaceInfo(
        id = placeId,
        name = placeName,
        address = placeAddress,
        roadAddress = placeRoadAddress,
        x = x,
        y = y
    ),
    visitDate = visitDate,
    noteTitle = noteTitle,
    noteContents = noteContents
)

fun List<PlaceNoteEntity>.toDomainModel() = map(PlaceNoteEntity::toDomainModel)

/**
 *  PlaceNote(Domain) -> PlaceNoteEntity(Data)
 */
fun PlaceNote.toEntity() = PlaceNoteEntity(
    placeImages = placeImages,
    placeId = placeInfo.id,
    placeName = placeInfo.name,
    placeAddress = placeInfo.address,
    placeRoadAddress = placeInfo.roadAddress,
    x = placeInfo.x,
    y = placeInfo.y,
    visitDate = visitDate,
    noteTitle = noteTitle,
    noteContents = noteContents
)
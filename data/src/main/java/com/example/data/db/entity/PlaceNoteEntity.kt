package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PlaceNoteModel

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
    val description: String,
    val placeName: String,
    val placeAddress: String,
    val x: Long,
    val y: Long,
    val startDate: Long,
    val endDate: Long
)

fun PlaceNoteEntity.toExternal() = PlaceNoteModel(
    id = id,
    placeImages = placeImages,
    description = description,
    placeName = placeName,
    placeAddress = placeAddress,
    x = x,
    y = y,
    startDate = startDate,
    endDate = endDate
)

fun List<PlaceNoteEntity>.toExternal() = map(PlaceNoteEntity::toExternal)
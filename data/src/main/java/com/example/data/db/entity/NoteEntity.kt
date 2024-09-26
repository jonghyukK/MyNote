package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NoteModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contents: String,
)

fun NoteEntity.toExternal() = NoteModel(
    id = id,
    contents = contents
)

fun List<NoteEntity>.toExternal() = map(NoteEntity::toExternal)
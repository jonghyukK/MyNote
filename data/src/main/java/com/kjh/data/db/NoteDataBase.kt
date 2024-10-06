package com.kjh.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kjh.data.db.entity.PlaceNoteEntity
import com.kjh.data.source.local.PlaceNoteDao

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

@Database(entities = [PlaceNoteEntity::class], version = 1, exportSchema = false)
@TypeConverters(DBTypeConverters::class)
abstract class NoteDataBase: RoomDatabase() {

    abstract fun placeNoteDao(): PlaceNoteDao
}
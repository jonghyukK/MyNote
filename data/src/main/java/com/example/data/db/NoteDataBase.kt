package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.source.NoteDao
import com.example.data.db.entity.NoteEntity

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NoteDataBase: RoomDatabase() {

    abstract fun noteDao(): NoteDao
}
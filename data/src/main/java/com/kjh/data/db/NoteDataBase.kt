package com.kjh.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.domain.model.PurchaseNote
import com.kjh.data.model.entity.PlaceNoteEntity
import com.kjh.data.model.entity.PurchaseNoteEntity
import com.kjh.data.source.local.PlaceNoteDao
import com.kjh.data.source.local.PurchaseNoteDao

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

@Database(entities = [PlaceNoteEntity::class, PurchaseNoteEntity::class], version = 2, exportSchema = false)
@TypeConverters(DBTypeConverters::class)
abstract class NoteDataBase: RoomDatabase() {

    abstract fun placeNoteDao(): PlaceNoteDao

    abstract fun purchaseNoteDao(): PurchaseNoteDao
}

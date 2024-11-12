package com.kjh.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kjh.data.db.NoteDataBase
import com.kjh.data.source.local.CategoryDao
import com.kjh.data.source.local.PlaceNoteDao
import com.kjh.data.source.local.PurchaseNoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): NoteDataBase {
        return Room.databaseBuilder(
            context.applicationContext,
            NoteDataBase::class.java,
            "Notes.db"
        )
            .addCallback(object: RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO categories (id, categoryName) VALUES (999, '기타')")
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun providePlaceNoteDao(dataBase: NoteDataBase): PlaceNoteDao = dataBase.placeNoteDao()

    @Provides
    @Singleton
    fun providePurchaseNoteDao(dataBase: NoteDataBase): PurchaseNoteDao = dataBase.purchaseNoteDao()

    @Provides
    @Singleton
    fun provideCategoryDao(dataBase: NoteDataBase): CategoryDao = dataBase.categoryDao()
}
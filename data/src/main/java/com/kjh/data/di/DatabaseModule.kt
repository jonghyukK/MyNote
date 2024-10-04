package com.kjh.data.di

import android.content.Context
import androidx.room.Room
import com.kjh.data.db.NoteDataBase
import com.kjh.data.source.PlaceNoteDao
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
        ).build()
    }

    @Provides
    fun providePlaceNoteDao(dataBase: NoteDataBase): PlaceNoteDao = dataBase.placeNoteDao()
}
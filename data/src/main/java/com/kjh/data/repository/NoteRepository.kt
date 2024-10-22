package com.kjh.data.repository

import com.kjh.data.db.entity.toEntity
import com.kjh.data.db.entity.toExternal
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.source.local.PlaceNoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 25..
 * Description:
 */

class NoteRepository @Inject constructor(
    private val noteLocalDataSource: PlaceNoteDao
) {

    fun observeAll(): Flow<List<PlaceNoteModel>> =
        noteLocalDataSource.observeAll().map { noteEntities ->
            noteEntities.toExternal()
        }

    suspend fun upsertPlaceNote(
        placeNoteModel: PlaceNoteModel,
        noteId: Int = -1,
    ): Flow<Result<PlaceNoteModel>> = flow {
        emit(Result.Loading)

        try {
            var placeNoteEntity = placeNoteModel.toEntity()
            if (noteId > 0) {
                placeNoteEntity = placeNoteEntity.copy(id = noteId)
            }

            val newNoteId = noteLocalDataSource.insert(placeNoteEntity)
            val newPlaceNote = noteLocalDataSource.getPlaceNoteById(newNoteId.toInt()).toExternal()

            emit(Result.Success(newPlaceNote))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }

    suspend fun getRecentVisitPlace(): Flow<Result<PlaceNoteModel>> = flow {
        emit(Result.Loading)

        try {
            val recentPlaceModel = noteLocalDataSource.getRecentVisitPlace()?.toExternal()
            emit(Result.Success(recentPlaceModel))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }

    suspend fun getPlacesInDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<Result<List<PlaceNoteModel>>> = flow {
        emit(Result.Loading)

        try {
            val places = noteLocalDataSource.getPlacesInDateRange(startDate, endDate).toExternal()
            emit(Result.Success(places))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }

    suspend fun getPlaceNoteById(noteId: Int): Flow<Result<PlaceNoteModel>> = flow {
        emit(Result.Loading)

        try {
            val placeNote = noteLocalDataSource.getPlaceNoteById(noteId).toExternal()
            emit(Result.Success(placeNote))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }

    suspend fun deletePlaceNoteById(noteId: Int): Flow<Result<Int>> = flow {
        emit(Result.Loading)

        try {
            noteLocalDataSource.deletePlaceNoteById(noteId)
            emit(Result.Success(noteId))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
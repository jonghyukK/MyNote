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

    suspend fun create(placeNoteModel: PlaceNoteModel): Flow<Result<Long>> = flow {
        emit(Result.Loading)

        try {
            val placeNoteEntity = placeNoteModel.toEntity()
            val newNoteId = noteLocalDataSource.insert(placeNoteEntity)

            if (newNoteId > 0) {
                emit(Result.Success(newNoteId))
            } else {
                throw Exception("Filed Insert PlaceNoteItem")
            }

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
}
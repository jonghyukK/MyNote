package com.kjh.data.repository

import com.kjh.data.db.entity.PlaceNoteEntity
import com.kjh.data.db.entity.toExternal
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.Result
import com.kjh.data.source.PlaceNoteDao
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
            val placeNoteEntity = PlaceNoteEntity(
                id = placeNoteModel.id,
                placeImages = placeNoteModel.placeImages,
                description = placeNoteModel.description,
                placeName = placeNoteModel.placeName,
                placeAddress = placeNoteModel.placeAddress,
                x = placeNoteModel.x,
                y = placeNoteModel.y,
                startDate = placeNoteModel.startDate,
                endDate = placeNoteModel.endDate
            )

            val newNoteId = noteLocalDataSource.insert(placeNoteEntity)
            emit(Result.Success(newNoteId))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
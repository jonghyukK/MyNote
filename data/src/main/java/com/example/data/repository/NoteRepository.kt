package com.example.data.repository

import com.example.data.db.entity.NoteEntity
import com.example.data.db.entity.toExternal
import com.example.data.model.NoteModel
import com.example.data.model.Result
import com.example.data.source.NoteDao
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
    private val noteLocalDataSource: NoteDao
) {

    fun observeAll(): Flow<List<NoteModel>> =
        noteLocalDataSource.observeAll().map { noteEntities ->
            noteEntities.toExternal()
        }

    suspend fun create(contents: String): Flow<Result<Long>> = flow {
        emit(Result.Loading)

        try {
            val newNoteId = noteLocalDataSource.insert(NoteEntity(contents = contents))
            emit(Result.Success(newNoteId))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }

    suspend fun getNoteById(noteId: Int): Flow<Result<NoteModel>> = flow {
        emit(Result.Loading)

        try {
            noteLocalDataSource.getNoteById(noteId)?.let { noteEntity ->
                emit(Result.Success(noteEntity.toExternal()))
            } ?: emit(Result.Error("Not Found Note by this Id: $noteId"))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
package com.example.domain.usecase

import com.example.domain.model.PlaceNote
import com.example.domain.model.Result
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
@Singleton
class UpsertAndGetPlaceNoteUseCase @Inject constructor(private val placeNoteRepository: PlaceNoteRepository) {

    suspend operator fun invoke(
        placeNote: PlaceNote,
        noteId: Int
    ): Flow<Result<PlaceNote>> = flow {
        emit(Result.Loading)

        try {
            val note = placeNoteRepository.upsertAndGetPlaceNote(placeNote, noteId)
            emit(Result.Success(note))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
package com.example.domain.usecase

import com.example.domain.model.PlaceNoteWithSamePlaceNameNotes
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
class GetPlaceNoteWithSamePlaceNameNotesUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {

    suspend operator fun invoke(noteId: Int): Flow<Result<PlaceNoteWithSamePlaceNameNotes>> = flow {
        emit(Result.Loading)

        try {
            val placeNote = placeNoteRepository.getPlaceNoteById(noteId)
            val samePlaceNameNotes = placeNoteRepository.getPlaceNotesByPlaceName(placeNote.placeName)
                .filter { it.id != noteId }

            emit(Result.Success(
                PlaceNoteWithSamePlaceNameNotes(
                    placeNote = placeNote,
                    samePlaceNameNotes = samePlaceNameNotes
                )
            ))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
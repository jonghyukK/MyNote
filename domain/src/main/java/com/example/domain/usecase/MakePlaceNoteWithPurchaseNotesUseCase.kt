package com.example.domain.usecase

import com.example.domain.model.PlaceNote
import com.example.domain.model.PurchaseNote
import com.example.domain.model.Result
import com.example.domain.repository.PlaceNoteRepository
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

class MakePlaceNoteWithPurchaseNotesUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository,
    private val purchaseNoteRepository: PurchaseNoteRepository
) {

    suspend operator fun invoke(
        placeNote: PlaceNote,
        purchaseNotes: List<PurchaseNote>,
        noteId: Int
    ): Flow<Result<PlaceNote>> = flow {
        emit(Result.Loading)

        try {
            purchaseNotes.map {
                purchaseNoteRepository.insertAndGetPurchaseNote(it)
            }

            val note = placeNoteRepository.upsertAndGetPlaceNote(placeNote, noteId)
            emit(Result.Success(note))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

class UpsertAndGetPlaceNoteUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {
    suspend operator fun invoke(
        placeNote: PlaceNote
    ): Flow<ApiResult<PlaceNote>> =
        safeApiCall {
            placeNoteRepository.upsertAndGetPlaceNote(placeNote)
        }
}
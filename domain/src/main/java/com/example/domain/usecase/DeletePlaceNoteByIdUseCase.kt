package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */

class DeletePlaceNoteByIdUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {
    suspend operator fun invoke(id: Int): Flow<ApiResult<Int>> =
        placeNoteRepository.deletePlaceNoteById(id)
}
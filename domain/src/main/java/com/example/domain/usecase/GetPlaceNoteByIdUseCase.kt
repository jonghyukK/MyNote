package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */

class GetPlaceNoteByIdUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository) {

    suspend operator fun invoke(id: Int): Flow<ApiResult<PlaceNote?>> =
        safeApiCall { placeNoteRepository.getPlaceNoteById(id) }
}
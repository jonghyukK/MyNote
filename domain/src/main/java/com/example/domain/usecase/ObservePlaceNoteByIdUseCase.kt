package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 12..
 * Description:
 */
class ObservePlaceNoteByIdUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {
    operator fun invoke(noteId: Int): Flow<ApiResult<PlaceNote?>> =
        placeNoteRepository.observePlaceNoteById(noteId)
}
package com.example.domain.usecase

import com.example.domain.model.PlaceNote
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
@Singleton
class GetPlaceNotesUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {

    operator fun invoke(): Flow<List<PlaceNote>> =
        placeNoteRepository.placeNotesFlow

}
package com.example.domain.usecase

import com.example.domain.model.FilteredSearchPlaceNotes
import com.example.domain.model.Result
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class GetFilteredSearchPlaceNotesUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {

    suspend operator fun invoke(
        query: String,
        startDate: Long,
        endDate: Long,
        isDescending: Boolean
    ): Flow<Result<List<FilteredSearchPlaceNotes>>> = flow {
        emit(Result.Loading)

        try {
            val notes = placeNoteRepository.getFilteredPlaceNotes(query, startDate, endDate, isDescending)
            emit(Result.Success(notes))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
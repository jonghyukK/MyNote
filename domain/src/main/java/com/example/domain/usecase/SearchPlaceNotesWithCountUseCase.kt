package com.example.domain.usecase

import com.example.domain.model.Result
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */
class SearchPlaceNotesWithCountUseCase @Inject constructor(
    private val notesRepository: PlaceNoteRepository
) {

    suspend operator fun invoke(query: String): Flow<Result<List<SearchPlaceNoteWithCount>>> = flow {
        emit(Result.Loading)

        try {
            val results = notesRepository.searchByQueryFlow(query)
            emit(Result.Success(results))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}

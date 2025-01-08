package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.SearchPlaceNoteWithCount
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */
class SearchPlaceNotesWithCountUseCase @Inject constructor(
    private val notesRepository: PlaceNoteRepository
) {
    suspend operator fun invoke(query: String): Flow<ApiResult<List<SearchPlaceNoteWithCount>>> =
        notesRepository.searchByQueryFlow(query)
}

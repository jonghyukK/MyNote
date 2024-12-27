package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.repository.PlaceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */
class GetPlaceNotesByDateRangeUseCase @Inject constructor(
    private val placeNoteRepository: PlaceNoteRepository
) {
    operator fun invoke(
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<List<PlaceNote>>> = placeNoteRepository.getPlaceNotesWithinDateRange(startDate, endDate)
}
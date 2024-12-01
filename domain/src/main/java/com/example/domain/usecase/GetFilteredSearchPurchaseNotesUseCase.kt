package com.example.domain.usecase

import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.Result
import com.example.domain.model.SortType
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */

class GetFilteredSearchPurchaseNotesUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {

    suspend operator fun invoke(
        queryText: String,
        startDate: Long,
        endDate: Long,
        minPrice: Long,
        maxPrice: Long,
        categoryIds: List<Int>,
        sortType: SortType
    ): Flow<Result<List<FilteredSearchPurchaseNotes>>> = flow {
        emit(Result.Loading)

        try {
            val notes = purchaseNoteRepository.getFilteredPurchaseNotes(
                queryText, startDate, endDate, minPrice, maxPrice, categoryIds, sortType
            )
            emit(Result.Success(notes))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
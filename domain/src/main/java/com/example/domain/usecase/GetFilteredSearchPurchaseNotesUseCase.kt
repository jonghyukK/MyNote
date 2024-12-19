package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.SortType
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
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
        queryText: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        categoryIds: List<Int> = emptyList(),
        sortType: SortType = SortType.LATEST
    ): Flow<ApiResult<List<FilteredSearchPurchaseNotes>>> =
        safeApiCall {
            purchaseNoteRepository.getFilteredPurchaseNotes(
                queryText,
                startDate,
                endDate,
                minPrice,
                maxPrice,
                categoryIds,
                sortType
            )
        }
}
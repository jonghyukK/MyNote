package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.PurchaseNameStats
import com.example.domain.model.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

class GetCategoryWithPurchaseNoteStatsUseCase @Inject constructor(
    private val getFilteredSearchPurchaseNotesUseCase: GetFilteredSearchPurchaseNotesUseCase,
    private val getPurchaseNameRankingsUseCase: GetPurchaseNameRankingsUseCase
) {
    suspend operator fun invoke(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<CategoryPurchaseNoteStats>> =
        combine(
            getFilteredSearchPurchaseNotesUseCase(
                startDate = startDate,
                endDate = endDate,
                categoryIds = listOf(categoryId)
            ),
            getPurchaseNameRankingsUseCase(
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate
            )
        ) { purchaseNotes, purchaseNameStats ->
            combineResults(purchaseNotes, purchaseNameStats)
        }


    private fun combineResults(
        result1: ApiResult<List<FilteredSearchPurchaseNotes>>,
        result2: ApiResult<List<PurchaseNameStats>>
    ): ApiResult<CategoryPurchaseNoteStats> =
        if (result1 is ApiResult.Success && result2 is ApiResult.Success) {
            val categoryTotalCount = result1.data.sumOf { it.purchaseNotes.size }
            val categoryTotalPrice = result1.data.sumOf { it.purchaseNotes.sumOf { it.purchasePrice } }
            val purchaseNameStatsList = result2.data
            val purchaseNoteList = result1.data

            ApiResult.Success(
                CategoryPurchaseNoteStats(
                    categoryTotalCount,
                    categoryTotalPrice,
                    purchaseNameStatsList,
                    purchaseNoteList
                )
            )
        } else {
            when {
                result1 is ApiResult.Error -> ApiResult.Error(result1.error)
                result2 is ApiResult.Error -> ApiResult.Error(result2.error)
                else -> ApiResult.Loading
            }
        }
}
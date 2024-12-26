package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

class GetCategoryStatisticsUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    operator fun invoke(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<CategoryPurchaseNoteStats>> =
        purchaseNoteRepository.getCategoryStatistics(categoryId, startDate, endDate)
}
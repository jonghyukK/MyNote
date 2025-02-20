package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryStatsDetail
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

class ObserveCategoryStatisticsUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    operator fun invoke(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<CategoryStatsDetail>> =
        purchaseNoteRepository.getCategoryStatistics(categoryId, startDate, endDate)
}
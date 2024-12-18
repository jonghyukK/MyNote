package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNameStats
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 16..
 * Description:
 */

class GetPurchaseNameRankingsUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<List<PurchaseNameStats>>> =
        safeApiCall { purchaseNoteRepository.getPurchaseNameRankings(categoryId, startDate, endDate) }
}
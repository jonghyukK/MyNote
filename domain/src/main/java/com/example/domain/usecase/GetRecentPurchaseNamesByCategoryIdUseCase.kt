package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 18..
 * Description:
 */
class GetRecentPurchaseNamesByCategoryIdUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(categoryId: Int?): Flow<ApiResult<List<String>>> =
        purchaseNoteRepository.getRecentPurchaseNamesByCategory(categoryId)
}
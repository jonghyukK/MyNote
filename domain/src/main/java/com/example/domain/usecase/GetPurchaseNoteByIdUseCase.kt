package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 31..
 * Description:
 */
class GetPurchaseNoteByIdUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(id: Int): Flow<ApiResult<PurchaseNote>> =
        safeApiCall {
            purchaseNoteRepository.getPurchaseNoteById(id)
        }
}
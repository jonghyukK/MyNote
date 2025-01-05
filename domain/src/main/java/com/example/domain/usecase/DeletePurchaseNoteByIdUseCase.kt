package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class DeletePurchaseNoteByIdUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(id: Int): Flow<ApiResult<Unit>> =
        purchaseNoteRepository.deletePurchaseNoteById(id)
}
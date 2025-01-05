package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class MakeAndGetPurchaseNoteUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(purchaseNote: PurchaseNote): Flow<ApiResult<PurchaseNote>> =
        purchaseNoteRepository.insertAndGetPurchaseNote(purchaseNote)
}
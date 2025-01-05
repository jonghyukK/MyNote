package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */
class GetPurchaseNoteByIdUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    operator fun invoke(id: Int): Flow<ApiResult<PurchaseNote>> =
        purchaseNoteRepository.getPurchaseNoteById(id)
}
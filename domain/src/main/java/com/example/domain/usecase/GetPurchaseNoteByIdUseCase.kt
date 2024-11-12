package com.example.domain.usecase

import com.example.domain.model.PurchaseNote
import com.example.domain.model.Result
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */
class GetPurchaseNoteByIdUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(id: Int): Flow<Result<PurchaseNote>> = flow {
        emit(Result.Loading)

        try {
            val purchaseNote = purchaseNoteRepository.getPurchaseNoteById(id)
            emit(Result.Success(purchaseNote))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
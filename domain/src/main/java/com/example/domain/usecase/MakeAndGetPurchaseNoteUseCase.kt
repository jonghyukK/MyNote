package com.example.domain.usecase

import com.example.domain.model.PurchaseNote
import com.example.domain.model.Result
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class MakeAndGetPurchaseNoteUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(purchaseNote: PurchaseNote): Flow<Result<PurchaseNote>> = flow {
        emit(Result.Loading)

        try {
            val newPurchaseNote = purchaseNoteRepository.insertAndGetPurchaseNote(purchaseNote)
            emit(Result.Success(newPurchaseNote))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
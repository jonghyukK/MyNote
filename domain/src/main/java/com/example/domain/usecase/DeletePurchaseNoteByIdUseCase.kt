package com.example.domain.usecase

import com.example.domain.model.Result
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 13..
 * Description:
 */
class DeletePurchaseNoteByIdUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(id: Int): Flow<Result<Unit>> = flow {
        emit(Result.Loading)

        try {
            purchaseNoteRepository.deletePurchaseNoteById(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
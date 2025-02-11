package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PlaceNote
import com.example.domain.model.PurchaseNote
import com.example.domain.model.safeApiCall
import com.example.domain.repository.TransactionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 12..
 * Description:
 */
class UpsertPlaceNoteAndMakePurchaseNotesUseCase @Inject constructor(
    private val transactionManager: TransactionManager
) {
    suspend operator fun invoke(
        placeNote: PlaceNote,
        purchaseNotes: List<PurchaseNote>
    ): Flow<ApiResult<PlaceNote>> =
        safeApiCall {
            transactionManager.upsertPlaceNoteAndInsertPurchaseNotes(placeNote, purchaseNotes)
        }
}


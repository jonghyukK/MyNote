package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PurchaseNote
import com.example.domain.model.safeApiCall
import com.example.domain.repository.TransactionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 21..
 * Description:
 */
class UpdateDefaultPaymentAndMakePurchaseNoteUseCase @Inject constructor(
    private val transactionManager: TransactionManager
) {
    suspend operator fun invoke(
        paymentMethod: PaymentMethod,
        purchaseNote: PurchaseNote
    ): Flow<ApiResult<PurchaseNote>> =
        safeApiCall {
            transactionManager.updatePaymentAndInsertPurchaseNote(paymentMethod, purchaseNote)
        }
}
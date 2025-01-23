package com.example.domain.repository

import com.example.domain.model.PaymentMethod
import com.example.domain.model.PurchaseNote

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 21..
 * Description:
*/
interface TransactionManager {

    suspend fun updatePaymentAndInsertPurchaseNote(
        paymentMethod: PaymentMethod,
        purchaseNote: PurchaseNote
    ): PurchaseNote
}
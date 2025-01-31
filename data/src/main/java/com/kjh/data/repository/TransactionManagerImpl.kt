package com.kjh.data.repository

import androidx.room.withTransaction
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PurchaseNote
import com.example.domain.repository.PaymentMethodRepository
import com.example.domain.repository.PurchaseNoteRepository
import com.example.domain.repository.TransactionManager
import com.kjh.data.db.NoteDataBase
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 21..
 * Description:
 */
class TransactionManagerImpl @Inject constructor(
    private val db: NoteDataBase,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val purchaseNoteRepository: PurchaseNoteRepository
): TransactionManager {

    override suspend fun updatePaymentAndInsertPurchaseNote(
        paymentMethod: PaymentMethod,
        purchaseNote: PurchaseNote,
    ): PurchaseNote {

        return db.withTransaction {
            paymentMethodRepository.upsertPaymentMethod(paymentMethod)
            purchaseNoteRepository.upsertAndGetPurchaseNote(purchaseNote)
        }
    }
}
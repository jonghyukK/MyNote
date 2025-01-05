package com.example.domain.usecase

import com.example.domain.model.PurchaseNote
import com.example.domain.repository.PurchaseNoteRepository
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 5..
 * Description:
 */
class MakeMultiplePurchaseNoteUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(purchaseNotes: List<PurchaseNote>) =
        purchaseNoteRepository.insertPurchaseNotes(purchaseNotes)
}
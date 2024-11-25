package com.example.domain.usecase

import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 25..
 * Description:
 */
class GetMaxPurchasePriceUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {

    operator fun invoke(): Flow<Long?> =
        purchaseNoteRepository.getMaxPurchasePrice
}
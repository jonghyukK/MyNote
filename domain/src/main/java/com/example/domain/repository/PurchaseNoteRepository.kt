package com.example.domain.repository

import com.example.domain.model.PurchaseNote
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */
interface PurchaseNoteRepository {

    val observeAll: Flow<List<PurchaseNote>>

    suspend fun insertPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote
}
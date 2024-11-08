package com.kjh.data.repository

import com.example.domain.model.PurchaseNote
import com.example.domain.repository.PurchaseNoteRepository
import com.kjh.data.model.entity.PurchaseNoteEntity
import com.kjh.data.model.entity.toDomainModel
import com.kjh.data.model.entity.toEntity
import com.kjh.data.source.local.PurchaseNoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */
class PurchaseNoteRepositoryImpl @Inject constructor(
    private val purchaseNoteLocalDateSource: PurchaseNoteDao
): PurchaseNoteRepository {

    override val observeAll: Flow<List<PurchaseNote>>
    get() = purchaseNoteLocalDateSource.observeAll().map(List<PurchaseNoteEntity>::toDomainModel)

    override suspend fun insertPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote {
        val insertedNoteId = purchaseNoteLocalDateSource.insert(purchaseNote.toEntity())
        val insertedNote = purchaseNoteLocalDateSource.getPurchaseNoteById(insertedNoteId.toInt())

        return insertedNote.toDomainModel()
    }
}
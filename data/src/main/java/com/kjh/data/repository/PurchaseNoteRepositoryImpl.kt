package com.kjh.data.repository

import com.example.domain.model.Category
import com.example.domain.model.PurchaseNote
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.PurchaseNoteRepository
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

    override suspend fun insertAndGetPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote {
        val purchaseNoteEntity = purchaseNote.toEntity()
        val newId = purchaseNoteLocalDateSource.insert(purchaseNoteEntity).toInt()

        return purchaseNoteLocalDateSource.getPurchaseNoteById(newId).toDomainModel()
    }

    override val getPurchaseNotesWithCategory: Flow<List<PurchaseNote>>
        get() = purchaseNoteLocalDateSource.getPurchaseNotesWithCategory()
            .map { data -> data.map { it.toDomainModel() } }

    override fun getPurchaseNotesByCategories(categories: List<Category>): Flow<List<PurchaseNote>> {
        val categoryIds = categories.map { it.id }
        return purchaseNoteLocalDateSource.getPurchaseNotesByCategoryIds(categoryIds)
            .map { data -> data.map { it.toDomainModel() } }
    }
}
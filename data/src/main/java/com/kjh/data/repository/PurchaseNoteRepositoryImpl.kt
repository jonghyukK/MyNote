package com.kjh.data.repository

import com.example.domain.model.Category
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.PurchaseNote
import com.example.domain.repository.PurchaseNoteRepository
import com.kjh.data.model.entity.toDomainModel
import com.kjh.data.model.entity.toEntity
import com.kjh.data.source.local.PurchaseNoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneOffset
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

    override suspend fun getPurchaseNoteById(id: Int): PurchaseNote {
        return purchaseNoteLocalDateSource.getPurchaseNoteById(id).toDomainModel()
    }

    override suspend fun deletePurchaseNoteById(id: Int) {
        purchaseNoteLocalDateSource.deletePurchaseNoteById(id)
    }

    override suspend fun getFilteredPurchaseNotes(
        queryText: String,
        startDate: Long,
        endDate: Long,
        minPrice: Long,
        maxPrice: Long,
        categoryIds: List<Int>
    ): List<FilteredSearchPurchaseNotes> {
        val purchaseNotes = purchaseNoteLocalDateSource.getFilteredPurchaseNotes(
            queryText, startDate, endDate, minPrice, maxPrice, categoryIds, categoryIdsSize = categoryIds.size
        )

        return purchaseNotes
            .groupBy { purchaseNote ->
                Instant.ofEpochMilli(purchaseNote.purchaseNote.purchaseDate)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
            }
            .map { (date, notes) ->
                FilteredSearchPurchaseNotes(
                    date = date,
                    purchaseNotes = notes.toDomainModel()
                )
            }
    }
}
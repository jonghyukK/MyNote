package com.kjh.data.repository

import androidx.room.withTransaction
import com.example.domain.repository.PurchaseNoteWithCategoryRepository
import com.kjh.data.db.NoteDataBase
import com.kjh.data.source.local.CategoryDao
import com.kjh.data.source.local.PurchaseNoteDao
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */
class PurchaseNoteWithCategoryRepositoryImpl @Inject constructor(
    private val purchaseNoteLocalDataSource: PurchaseNoteDao,
    private val categoryLocalDataSource: CategoryDao,
    private val database: NoteDataBase
): PurchaseNoteWithCategoryRepository {

    override suspend fun deleteCategoryAndReassignETC(categoryId: Int) {
        database.withTransaction {
            purchaseNoteLocalDataSource.updateCategoryIdForPurchaseNote(999, categoryId)
            categoryLocalDataSource.deleteCategoryById(categoryId)
        }
    }
}
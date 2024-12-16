package com.example.domain.repository

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */
interface PurchaseNoteWithCategoryRepository {

    suspend fun deleteCategoryAndReassignETC(categoryId: Int)
}
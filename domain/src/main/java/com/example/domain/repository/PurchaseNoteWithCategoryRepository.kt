package com.example.domain.repository

import com.example.domain.model.CategoryWithPurchaseDetails

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */
interface PurchaseNoteWithCategoryRepository {

    suspend fun deleteCategoryAndReassignETC(categoryId: Int)

    suspend fun getCategoryWithPurchaseDetails(): List<CategoryWithPurchaseDetails>
}
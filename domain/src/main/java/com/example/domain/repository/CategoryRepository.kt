package com.example.domain.repository

import com.example.domain.model.Category
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.model.CategoryWithStats
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
interface CategoryRepository {

    fun allCategoriesFlow(): Flow<List<Category>>

    fun getCategoriesWithPurchaseNoteCount(): Flow<List<CategoryWithPurchaseNoteCount>>

    suspend fun insertCategory(category: Category): Long

    suspend fun getCategoryByName(name: String): Category?

    suspend fun getCategoryById(id: Int): Category?

    suspend fun updateCategoryName(category: Category)

    suspend fun deleteCategoryById(id: Int)

    fun getCategoriesWithStats(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryWithStats>>
}
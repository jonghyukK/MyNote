package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.CategoryWithPurchaseNoteCount
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
interface CategoryRepository {

    fun allCategoriesFlow(): Flow<ApiResult<List<Category>>>

    fun getCategoriesWithNoteCounts(
        startDate: Long?,
        endDate: Long?
    ): Flow<ApiResult<List<CategoryWithPurchaseNoteCount>>>

    suspend fun upsertCategory(category: Category): Flow<ApiResult<Long>>

    suspend fun deleteCategoryById(id: Int): Flow<ApiResult<Unit>>

    suspend fun getCategoryByName(name: String): Category?

    suspend fun getCategoryById(id: Int): Category?
}
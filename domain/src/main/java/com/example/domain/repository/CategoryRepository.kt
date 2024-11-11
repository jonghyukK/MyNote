package com.example.domain.repository

import com.example.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
interface CategoryRepository {

    fun allCategoriesFlow(): Flow<List<Category>>

    suspend fun insertCategory(category: Category): Long

    suspend fun getCategoryByName(name: String): Category?

    suspend fun getCategoryById(id: Int): Category?

    suspend fun updateCategoryName(category: Category)

    suspend fun deleteCategoryById(id: Int)
}
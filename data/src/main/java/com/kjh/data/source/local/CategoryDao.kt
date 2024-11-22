package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.domain.model.CategoryWithPurchaseDetails
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.model.PurchaseDetail
import com.kjh.data.model.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE categoryName = :categoryName")
    suspend fun getCategoryByName(categoryName: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Query("UPDATE categories SET categoryName = :newName WHERE id = :id")
    suspend fun updateCategoryName(id: Int, newName: String)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("""
        SELECT
        c.id AS categoryId,
        c.categoryName AS categoryName,
        COUNT(p.id) AS purchaseNoteCount
        FROM categories c
        LEFT JOIN purchase p ON c.id = p.categoryId
        GROUP BY c.id
    """)
    fun getCategoriesWithPurchaseNoteCount(): Flow<List<CategoryWithPurchaseNoteCount>>

    @Query("""
        SELECT c.id AS categoryId,
        c.categoryName AS categoryName,
        COUNT(p.id) AS purchaseNoteCount
        FROM categories c
        LEFT JOIN purchase p ON c.id = p.categoryId
        GROUP BY c.id, c.categoryName
    """)
    suspend fun getCategoryPurchaseCounts(): List<CategoryWithPurchaseNoteCount>

    @Query("""
        SELECT p.purchaseName AS purchaseName,
        COUNT(p.id) AS count
        FROM purchase p
        WHERE p.categoryId = :categoryId
        GROUP BY p.purchaseName
    """)
    suspend fun getPurchaseDetailsByCategory(categoryId: Int): List<PurchaseDetail>
}
package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.model.CategoryWithStats
import com.kjh.data.model.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@Dao
interface CategoryDao {

    /**
     *  모든 카테고리 목록 조회.
     *
     * @return Flow<List<CategoryEntity>>
     */
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * 카테고리 등록.
     *
     * @param category
     * @return Long (등록된 카테고리 id)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    /**
     * 카테고리 조회 by CategoryName
     *
     * @param categoryName
     * @return CategoryEntity?
     */
    @Query("SELECT * FROM categories WHERE categoryName = :categoryName")
    suspend fun getCategoryByName(categoryName: String): CategoryEntity?

    /**
     * 카테고리 조회 by categoryId
     *
     * @param id
     * @return CategoryEntity?
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    /**
     * 카테고리명 수정 by categoryId.
     *
     * @param id
     * @param newName
     */
    @Query("UPDATE categories SET categoryName = :newName WHERE id = :id")
    suspend fun updateCategoryName(id: Int, newName: String)

    /**
     * 카테고리 삭제 by categoryId.
     *
     * @param id
     */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    /**
     * 카테고리 목록과 각 카테고리별 구매노트 Count 조회.
     *
     * @return Flow<List<CategoryWithPurchaseNoteCount>>
     */
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
        SELECT
            categories.id AS categoryId,
            categories.categoryName AS categoryName,
            COUNT(purchase.id) AS purchaseNoteTotalCount,
            COALESCE(SUM(purchase.purchasePrice), 0) AS purchaseNoteTotalPrice
        FROM
            categories
        LEFT JOIN
            purchase ON categories.id = purchase.categoryId
        WHERE
            (:startDate IS NULL OR purchase.purchaseDate >= :startDate) AND
            (:endDate IS NULL OR purchase.purchaseDate <= :endDate)
        GROUP BY
            categories.id, categories.categoryName
    """)
    fun getCategoriesWithStatsByDate(startDate: Long?, endDate: Long?): Flow<List<CategoryWithStats>>

}
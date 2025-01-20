package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.domain.model.CategoryWithPurchaseNoteCount
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
     * 카테고리 등록 or 수정
     *
     * @param category
     * @return 등록 or 수정된 카테고리 id.
     */
    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

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
     * 카테고리 삭제 by categoryId.
     *
     * @param id
     */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    /**
     * 카테고리 목록 조회 및 각 카테고리별 구매노트 카운트 조회.
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("""
    SELECT 
        c.id AS categoryId,
        c.categoryName AS categoryName,
        COUNT(p.id) AS purchaseNoteCount
    FROM 
        categories c
    LEFT JOIN 
        purchase p ON c.id = p.categoryId
        AND (:startDate IS NULL OR p.purchaseDate >= :startDate)
        AND (:endDate IS NULL OR p.purchaseDate <= :endDate)
    GROUP BY 
        c.id, c.categoryName
""")
    fun getCategoriesWithPurchaseNoteCount(
        startDate: Long?,
        endDate: Long?
    ): Flow<List<CategoryWithPurchaseNoteCount>>
}
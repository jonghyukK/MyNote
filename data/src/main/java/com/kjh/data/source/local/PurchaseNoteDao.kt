package com.kjh.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.domain.model.CategoryStats
import com.example.domain.model.PaymentMethodStats
import com.example.domain.model.PurchaseNameStats
import com.example.domain.model.SortType
import com.kjh.data.model.PurchaseNoteModel
import com.kjh.data.model.dto.PurchaseNoteTotalStatsDto
import com.kjh.data.model.entity.PurchaseNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@Dao
interface PurchaseNoteDao {

    @Transaction
    @Query("SELECT * FROM purchase")
    fun getAllPurchaseNotes(): Flow<List<PurchaseNoteModel>>

    @Transaction
    @Query("SELECT * FROM purchase WHERE categoryId IN (:categoryIds)")
    fun getPurchaseNotesByCategoryIds(categoryIds: List<Int>): Flow<List<PurchaseNoteModel>>

    @Transaction
    @Query("SELECT * FROM purchase WHERE id = :id")
    fun getPurchaseNoteFlowById(id: Int): Flow<PurchaseNoteModel>

    @Transaction
    @Query("SELECT * FROM purchase WHERE id = :id")
    suspend fun getPurchaseNoteById(id: Int): PurchaseNoteModel

    @Transaction
    @Query("""
        SELECT * FROM purchase
        WHERE purchaseDate = :purchaseDate AND placeName = :placeName
        """)
    suspend fun getPurchaseNotesByDateAndPlaceName(
        purchaseDate: Long, placeName: String,
    ): List<PurchaseNoteModel>

    @Transaction
    @Query("""
        SELECT * FROM purchase
        WHERE 
            (:queryText IS NULL OR purchaseName LIKE '%' || :queryText || '%') AND
        (:startDate IS NULL OR purchaseDate >= :startDate) AND
        (:endDate IS NULL OR purchaseDate <= :endDate) AND
        (:minPrice IS NULL OR purchasePrice >= :minPrice) AND
        (:maxPrice IS NULL OR purchasePrice <= :maxPrice) AND
        (:categoryIdsSize = 0 OR categoryId IN (:categoryIds)) AND
        (:paymentMethodSize = 0 OR paymentMethodId IN (:paymentMethodIds))
        ORDER BY
        CASE WHEN :sortType = 'LATEST' THEN purchaseDate END DESC,
        CASE WHEN :sortType = 'OLDEST' THEN purchaseDate END ASC,
        CASE WHEN :sortType = 'HIGH_PRICE' THEN purchasePrice END DESC,
        CASE WHEN :sortType = 'LOW_PRICE' THEN purchasePrice END ASC
    """)
    fun getFilteredPurchaseNotes(
        queryText: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        categoryIds: List<Int> = emptyList(),
        categoryIdsSize: Int = 0,
        paymentMethodIds: List<Int> = emptyList(),
        paymentMethodSize: Int = 0,
        sortType: String = SortType.LATEST.name
    ): Flow<List<PurchaseNoteModel>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseNote(purchaseNoteEntity: PurchaseNoteEntity): Long

    @Insert
    suspend fun insertPurchaseNotes(notes: List<PurchaseNoteEntity>)

    @Query("UPDATE purchase SET categoryId = :etcCategoryId WHERE categoryId = :categoryId")
    suspend fun updateCategoryIdForPurchaseNote(etcCategoryId: Int, categoryId: Int)

    @Query("DELETE FROM purchase WHERE id = :id")
    suspend fun deletePurchaseNoteById(id: Int)

    @Query("SELECT MAX(purchasePrice) FROM purchase")
    fun getMaxPurchasePrice(): Flow<Long?>

    /**
     * 카테고리 목록 및 목록별 구매노트 총 갯수, 총 가격 조회.
     *
     * @param startDate
     * @param endDate
     * @return Flow<List<CategoryStats>>
     */
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
    fun getCategoryStatsByDate(startDate: Long?, endDate: Long?): Flow<List<CategoryStats>>

    @Query("""
        SELECT
            paymentMethod.paymentMethodId AS paymentMethodId,
            paymentMethod.paymentMethodName AS paymentMethodName,
            COUNT(purchase.id) AS purchaseNoteTotalCount,
            COALESCE(SUM(purchase.purchasePrice), 0) AS purchaseNoteTotalPrice
        FROM 
            paymentMethod
        LEFT JOIN
            purchase ON paymentMethod.paymentMethodId = purchase.paymentMethodId
        WHERE
            (:startDate IS NULL OR purchase.purchaseDate >= :startDate) AND
            (:endDate IS NULL OR purchase.purchaseDate <= :endDate)
        GROUP BY
            paymentMethod.paymentMethodId, paymentMethod.paymentMethodName
    """)
    fun getPaymentMethodStatsByDate(startDate: Long?, endDate: Long?): Flow<List<PaymentMethodStats>>

    /**
     * 구매노트 총 갯수, 총 가격 조회.
     *
     * @param categoryId null이면 전체를 조회.
     * @param startDate 시작 날짜 (null이면 조건 무시).
     * @param endDate 종료 날짜 (null이면 조건 무시).
     * @return 구매노트 총 갯수, 총 가격
     */
    @Query("""
    SELECT 
        COUNT(*) AS totalCount,
        COALESCE(SUM(purchasePrice), 0) AS totalPrice
    FROM 
        purchase
    WHERE 
        (:categoryId IS NULL OR categoryId = :categoryId) AND
        (:startDate IS NULL OR purchaseDate >= :startDate) AND
        (:endDate IS NULL OR purchaseDate <= :endDate)
""")
    fun getPurchaseNoteTotalStats(
        categoryId: Int? = null,
        startDate: Long?,
        endDate: Long?
    ): Flow<PurchaseNoteTotalStatsDto>

    /**
     * 특정 카테고리의 구매명 목록 및 카운트 조회.
     *
     * @param categoryId
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("""
    SELECT 
        p.purchaseName AS purchaseName,
        COUNT(*) AS totalCount,
        COALESCE(SUM(p.purchasePrice), 0) AS totalPrice
    FROM 
        purchase p
    WHERE 
        (:categoryId IS NULL OR p.categoryId = :categoryId) AND
        (:startDate IS NULL OR p.purchaseDate >= :startDate) AND
        (:endDate IS NULL OR p.purchaseDate <= :endDate)
    GROUP BY 
        p.purchaseName
    ORDER BY 
        totalCount DESC
""")
    fun getPurchaseNameStats(
        categoryId: Int?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<PurchaseNameStats>>

    /**
     * 특정 카테고리의 최근 등록된 구매명 목록 조회.
     *
     * @param categoryId
     * @return
     */
    @Query("""
        SELECT DISTINCT purchaseName 
        FROM purchase 
        WHERE categoryId = :categoryId
        ORDER BY purchaseDate DESC 
        LIMIT 10
    """)
    suspend fun getRecentPurchaseNamesByCategory(categoryId: Int?): List<String>
}
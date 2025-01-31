package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchaseNoteStatistics
import com.example.domain.model.SortType
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */
interface PurchaseNoteRepository {

    fun getAllPurchaseNotes(): Flow<ApiResult<List<PurchaseNote>>>

    fun getPurchaseNotesByCategories(categories: List<Category>): Flow<List<PurchaseNote>>

    fun observePurchaseNoteById(id: Int): Flow<PurchaseNote>

    suspend fun getPurchaseNoteById(id: Int): PurchaseNote

    suspend fun getPurchaseNotesByPlaceAndDate(
        placeName: String,
        date: Long
    ): List<PurchaseNote>

    fun getFilteredPurchaseNotes(
        queryText: String?,
        startDate: Long?,
        endDate: Long?,
        minPrice: Long?,
        maxPrice: Long?,
        categoryIds: List<Int>,
        paymentMethodIds: List<Int>,
        sortType: SortType
    ): Flow<ApiResult<List<PurchaseNote>>>

    suspend fun insertPurchaseNotes(purchaseNotes: List<PurchaseNote>): Flow<ApiResult<Unit>>

    suspend fun upsertAndGetPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote

    suspend fun deletePurchaseNoteById(id: Int): Flow<ApiResult<Unit>>

    fun getMaxPurchasePrice(): Flow<ApiResult<Long?>>

    fun getPurchaseNotesStatistics(
        startDate: Long?,
        endDate: Long?
    ): Flow<ApiResult<PurchaseNoteStatistics>>

    fun getCategoryStatistics(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<CategoryPurchaseNoteStats>>

    suspend fun getRecentPurchaseNamesByCategory(categoryId: Int?): List<String>
}
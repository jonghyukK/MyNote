package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.CategoryStatsDetail
import com.example.domain.model.MonthlyWeekInfo
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

    fun observePurchaseNoteById(id: Int): Flow<ApiResult<PurchaseNote?>>

    fun observePurchaseNotesByPlaceAndDate(
        placeName: String,
        date: Long
    ): Flow<ApiResult<List<PurchaseNote>>>

    suspend fun getPurchaseNoteById(id: Int): PurchaseNote

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

    suspend fun insertPurchaseNotes(purchaseNotes: List<PurchaseNote>)

    suspend fun upsertAndGetPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote

    suspend fun deletePurchaseNoteById(id: Int): Flow<ApiResult<Unit>>

    fun getMaxPurchasePrice(): Flow<ApiResult<Long?>>

    fun getPurchaseNotesStatistics(
        startDate: Long,
        endDate: Long,
        monthlyWeekInfoList: List<MonthlyWeekInfo>
    ): Flow<ApiResult<PurchaseNoteStatistics>>

    fun getCategoryStatistics(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<CategoryStatsDetail>>

    suspend fun getRecentPurchaseNamesByCategory(categoryId: Int?): List<String>
}
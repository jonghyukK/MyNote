package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.FilteredSearchPurchaseNotes
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

    fun getAllPurchaseNotes(): Flow<List<PurchaseNote>>

    fun getPurchaseNotesByCategories(categories: List<Category>): Flow<List<PurchaseNote>>

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
        sortType: SortType
    ): Flow<ApiResult<List<FilteredSearchPurchaseNotes>>>

    suspend fun insertAndGetPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote

    suspend fun deletePurchaseNoteById(id: Int)

    fun getMaxPurchasePrice(): Flow<Long?>

    fun getPurchaseNotesStatistics(
        startDate: Long?,
        endDate: Long?
    ): Flow<ApiResult<PurchaseNoteStatistics>>

    fun getCategoryStatistics(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<ApiResult<CategoryPurchaseNoteStats>>
}
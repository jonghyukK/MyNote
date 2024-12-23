package com.example.domain.repository

import com.example.domain.model.Category
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.PurchaseNameStats
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

    suspend fun insertAndGetPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote

    val getPurchaseNotesWithCategory: Flow<List<PurchaseNote>>

    fun getPurchaseNotesByCategories(categories: List<Category>): Flow<List<PurchaseNote>>

    suspend fun getPurchaseNoteById(id: Int): PurchaseNote

    suspend fun deletePurchaseNoteById(id: Int)

    suspend fun getFilteredPurchaseNotes(
        queryText: String?,
        startDate: Long?,
        endDate: Long?,
        minPrice: Long?,
        maxPrice: Long?,
        categoryIds: List<Int>,
        sortType: SortType
    ): List<FilteredSearchPurchaseNotes>

    val getMaxPurchasePrice: Flow<Long?>

    suspend fun getPurchaseNotesByPlaceAndDate(
        placeName: String,
        date: Long
    ): List<PurchaseNote>

    suspend fun getPurchaseNameRankings(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): List<PurchaseNameStats>

    fun getPurchaseNotesStatistics(
        startDate: Long?,
        endDate: Long?
    ): Flow<PurchaseNoteStatistics>
}
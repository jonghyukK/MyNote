package com.kjh.data.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.FilteredSearchPurchaseNotes
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchaseNoteStatistics
import com.example.domain.model.SortType
import com.example.domain.model.asResult
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PurchaseNoteRepository
import com.kjh.data.model.entity.toEntity
import com.kjh.data.model.toDomainModel
import com.kjh.data.source.local.PurchaseNoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */
class PurchaseNoteRepositoryImpl @Inject constructor(
    private val purchaseNoteLocalDateSource: PurchaseNoteDao
): PurchaseNoteRepository {

    override fun getAllPurchaseNotes(): Flow<List<PurchaseNote>> =
        purchaseNoteLocalDateSource.getAllPurchaseNotes()
            .map { data -> data.map { it.toDomainModel() } }

    override fun getPurchaseNotesByCategories(categories: List<Category>): Flow<List<PurchaseNote>> {
        val categoryIds = categories.map { it.id }
        return purchaseNoteLocalDateSource.getPurchaseNotesByCategoryIds(categoryIds)
            .map { data -> data.map { it.toDomainModel() } }
    }

    override fun getPurchaseNoteById(id: Int): Flow<ApiResult<PurchaseNote>> =
        purchaseNoteLocalDateSource.getPurchaseNoteFlowById(id)
            .map { data -> data.toDomainModel() }
            .asResult()

    override suspend fun getPurchaseNotesByPlaceAndDate(
        placeName: String,
        date: Long,
    ): List<PurchaseNote> =
        purchaseNoteLocalDateSource.getPurchaseNotesByDateAndPlaceName(
            purchaseDate = date,
            placeName = placeName
        ).toDomainModel()

    /**
     *  조건에 맞는 구매노트 목록 조회.
     *
     * @param queryText
     * @param startDate
     * @param endDate
     * @param minPrice
     * @param maxPrice
     * @param categoryIds
     * @param sortType
     * @return
     */
    override fun getFilteredPurchaseNotes(
        queryText: String?,
        startDate: Long?,
        endDate: Long?,
        minPrice: Long?,
        maxPrice: Long?,
        categoryIds: List<Int>,
        sortType: SortType
    ): Flow<ApiResult<List<FilteredSearchPurchaseNotes>>> =
        purchaseNoteLocalDateSource.getFilteredPurchaseNotes(
            queryText,
            startDate,
            endDate,
            minPrice,
            maxPrice,
            categoryIds,
            categoryIdsSize = categoryIds.size,
            sortType.name
        ).map { filteredNotes ->
            when (sortType) {
                SortType.HIGH_PRICE, SortType.LOW_PRICE -> listOf(
                    FilteredSearchPurchaseNotes(null, filteredNotes.toDomainModel())
                )
                else -> {
                    filteredNotes
                        .groupBy { purchaseNote ->
                            Instant.ofEpochMilli(purchaseNote.purchaseNote.purchaseDate)
                                .atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        .map { (date, notes) ->
                            FilteredSearchPurchaseNotes(date, notes.toDomainModel())
                        }
                }
            }
        }.asResult()

    override suspend fun insertPurchaseNotes(purchaseNotes: List<PurchaseNote>): Flow<ApiResult<Unit>> =
        safeApiCall {
            purchaseNoteLocalDateSource.insertPurchaseNotes(purchaseNotes.map { it.toEntity() })
        }

    override suspend fun insertAndGetPurchaseNote(purchaseNote: PurchaseNote): Flow<ApiResult<PurchaseNote>> =
        safeApiCall {
            val purchaseNoteEntity = purchaseNote.toEntity()
            val newId = purchaseNoteLocalDateSource.insertPurchaseNote(purchaseNoteEntity).toInt()

            purchaseNoteLocalDateSource.getPurchaseNoteById(newId).toDomainModel()
        }

    override suspend fun deletePurchaseNoteById(id: Int): Flow<ApiResult<Unit>> =
        safeApiCall {
            purchaseNoteLocalDateSource.deletePurchaseNoteById(id)
        }


    override fun getMaxPurchasePrice(): Flow<Long?> =
        purchaseNoteLocalDateSource.getMaxPurchasePrice()

    /**
     * 구매노트 통계 데이터 조회.
     *
     * @param startDate
     * @param endDate
     * @return
     */
    override fun getPurchaseNotesStatistics(
        startDate: Long?,
        endDate: Long?,
    ): Flow<ApiResult<PurchaseNoteStatistics>> {
        val purchaseNoteTotalStats =
            purchaseNoteLocalDateSource.getPurchaseNoteTotalStats(startDate = startDate, endDate = endDate)
        val categoryStatsList =
            purchaseNoteLocalDateSource.getCategoryStatsByDate(startDate = startDate, endDate = endDate)

        return combine(
            purchaseNoteTotalStats,
            categoryStatsList
        ) { totalStats, categoryStats ->
            PurchaseNoteStatistics(
                totalNoteCount = totalStats.totalCount,
                totalPurchasePrice = totalStats.totalPrice,
                categoryStatsList = categoryStats
                    .filter { it.purchaseNoteTotalCount > 0 }
                    .sortedByDescending { it.purchaseNoteTotalCount }
            )
        }.asResult()
    }

    /**
     * 구매노트 카테고리 통계 데이터 조회.
     *
     * @param categoryId
     * @param startDate
     * @param endDate
     * @return
     */
    override fun getCategoryStatistics(
        categoryId: Int,
        startDate: Long,
        endDate: Long,
    ): Flow<ApiResult<CategoryPurchaseNoteStats>> {
        val purchaseNameStats =
            purchaseNoteLocalDateSource.getPurchaseNameStats(categoryId, startDate, endDate)
        val purchaseNoteTotalStats =
            purchaseNoteLocalDateSource.getPurchaseNoteTotalStats(categoryId, startDate, endDate)

        return combine(
            purchaseNoteTotalStats,
            purchaseNameStats
        ) { totalStats, purchaseNameStatsList ->
            CategoryPurchaseNoteStats(
                categoryTotalCount = totalStats.totalCount,
                categoryTotalPrice = totalStats.totalPrice,
                purchaseNameStats = purchaseNameStatsList
            )
        }.asResult()
    }
}
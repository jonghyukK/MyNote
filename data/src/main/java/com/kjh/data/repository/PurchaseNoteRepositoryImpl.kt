package com.kjh.data.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryPurchaseNoteStats
import com.example.domain.model.CategoryStatsDetail
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
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */
class PurchaseNoteRepositoryImpl @Inject constructor(
    private val purchaseNoteLocalDataSource: PurchaseNoteDao
): PurchaseNoteRepository {

    override fun observePurchaseNoteById(id: Int): Flow<ApiResult<PurchaseNote?>> =
        purchaseNoteLocalDataSource.getPurchaseNoteFlowById(id)
            .map { data -> data?.toDomainModel() }
            .asResult()

    override fun observePurchaseNotesByPlaceAndDate(
        placeName: String,
        date: Long,
    ): Flow<ApiResult<List<PurchaseNote>>> =
        purchaseNoteLocalDataSource.getPurchaseNotesByDateAndPlaceNameFlow(date, placeName)
            .map { it.toDomainModel() }
            .asResult()

    override suspend fun getPurchaseNoteById(id: Int): PurchaseNote =
        purchaseNoteLocalDataSource.getPurchaseNoteById(id).toDomainModel()

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
        paymentMethodIds: List<Int>,
        sortType: SortType
    ): Flow<ApiResult<List<PurchaseNote>>> =
        purchaseNoteLocalDataSource.getFilteredPurchaseNotes(
            queryText,
            startDate,
            endDate,
            minPrice,
            maxPrice,
            categoryIds,
            categoryIdsSize = categoryIds.size,
            paymentMethodIds,
            paymentMethodSize = paymentMethodIds.size,
            sortType.name
        ).map { it.toDomainModel() }
            .asResult()

    override suspend fun insertPurchaseNotes(purchaseNotes: List<PurchaseNote>) =
        purchaseNoteLocalDataSource.insertPurchaseNotes(purchaseNotes.map { it.toEntity() })

    override suspend fun upsertAndGetPurchaseNote(purchaseNote: PurchaseNote): PurchaseNote {
        val purchaseNoteEntity = purchaseNote.toEntity()
        var upsertId = purchaseNoteLocalDataSource.upsertPurchaseNote(purchaseNoteEntity).toInt()

        if (upsertId == -1) {
            upsertId = purchaseNote.id
        }

        return purchaseNoteLocalDataSource.getPurchaseNoteById(upsertId).toDomainModel()
    }

    override suspend fun deletePurchaseNoteById(id: Int): Flow<ApiResult<Unit>> =
        safeApiCall {
            purchaseNoteLocalDataSource.deletePurchaseNoteById(id)
        }


    override fun getMaxPurchasePrice(): Flow<ApiResult<Long?>> =
        purchaseNoteLocalDataSource.getMaxPurchasePrice().asResult()

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
            purchaseNoteLocalDataSource.getPurchaseNoteTotalStats(startDate = startDate, endDate = endDate)
        val categoryStatsList =
            purchaseNoteLocalDataSource.getCategoryStatsListByDate(startDate = startDate, endDate = endDate)
        val paymentMethodStatsList =
            purchaseNoteLocalDataSource.getPaymentMethodStatsListByDate(startDate = startDate, endDate = endDate)

        return combine(
            purchaseNoteTotalStats,
            categoryStatsList,
            paymentMethodStatsList
        ) { totalStats, categoryStats, paymentStats ->
            PurchaseNoteStatistics(
                totalNoteCount = totalStats.totalCount,
                totalPurchasePrice = totalStats.totalPrice,
                categoryStatsList = categoryStats
                    .filter { it.purchaseNoteTotalCount > 0 }
                    .sortedByDescending { it.purchaseNoteTotalPrice },
                paymentMethodStatsList = paymentStats
                    .filter { it.purchaseNoteTotalCount > 0 }
                    .sortedByDescending { it.purchaseNoteTotalPrice }
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
    ): Flow<ApiResult<CategoryStatsDetail>> {
        val categoryStatsResult =
            purchaseNoteLocalDataSource.getCategoryStatsByDate(categoryId, startDate, endDate)
        val purchaseNameStatsListResult =
            purchaseNoteLocalDataSource.getPurchaseNameStatsList(categoryId, startDate, endDate)

        return combine(
            categoryStatsResult,
            purchaseNameStatsListResult
        ) { categoryStats, purchaseNameStatsList ->
            CategoryStatsDetail(
                categoryStats = categoryStats,
                purchaseNameStatsList = purchaseNameStatsList
            )
        }.asResult()
    }

    override suspend fun getRecentPurchaseNamesByCategory(categoryId: Int?): List<String> =
        purchaseNoteLocalDataSource.getRecentPurchaseNamesByCategory(categoryId)

}
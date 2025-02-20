package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNoteStatistics
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */
class ObservePurchaseNoteStatisticsByDateUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<ApiResult<PurchaseNoteStatistics>> =
        purchaseNoteRepository.getPurchaseNotesStatistics(startDate, endDate)
}
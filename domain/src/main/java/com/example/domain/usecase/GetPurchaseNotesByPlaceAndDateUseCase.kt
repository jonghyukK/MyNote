package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PurchaseNote
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 5..
 * Description:
 */
class GetPurchaseNotesByPlaceAndDateUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    suspend operator fun invoke(placeName: String, date: Long): Flow<ApiResult<List<PurchaseNote>>> =
        safeApiCall { purchaseNoteRepository.getPurchaseNotesByPlaceAndDate(placeName, date) }
}
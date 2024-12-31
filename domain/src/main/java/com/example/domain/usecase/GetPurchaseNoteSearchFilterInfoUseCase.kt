package com.example.domain.usecase

import com.example.domain.model.PurchaseNoteSearchFilterInfo
import com.example.domain.repository.PurchaseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 2..
 * Description:
 */
class GetPurchaseNoteSearchFilterInfoUseCase @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    operator fun invoke(): Flow<PurchaseNoteSearchFilterInfo> {
        return combine(
            getAllCategoriesUseCase(),
            purchaseNoteRepository.getMaxPurchasePrice()
        ) { allCategories, maxPurchasePrice ->
            PurchaseNoteSearchFilterInfo(
                categories = allCategories,
                maxPrice = maxPurchasePrice
            )
        }
    }
}
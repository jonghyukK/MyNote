package com.example.domain.usecase

import com.example.domain.model.Category
import com.example.domain.repository.PurchaseNoteRepository
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 12..
 * Description:
 */
class GetPurchaseNotesByCategoriesUseCase @Inject constructor(
    private val purchaseNoteRepository: PurchaseNoteRepository
) {
    operator fun invoke(categories: List<Category>) =
        purchaseNoteRepository.getPurchaseNotesByCategories(categories)
}
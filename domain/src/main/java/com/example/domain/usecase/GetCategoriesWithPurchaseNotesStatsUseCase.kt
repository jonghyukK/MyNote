package com.example.domain.usecase

import com.example.domain.model.CategoryWithPurchaseNotesCountAndTotalPrice
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
class GetCategoriesWithPurchaseNotesStatsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    operator fun invoke(): Flow<List<CategoryWithPurchaseNotesCountAndTotalPrice>> =
        categoryRepository.getCategoriesWithPurchaseNotesCountAndTotalPrice()
}
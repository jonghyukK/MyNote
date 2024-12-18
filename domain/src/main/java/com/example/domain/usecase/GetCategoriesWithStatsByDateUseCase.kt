package com.example.domain.usecase

import com.example.domain.model.CategoryWithStats
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */

class GetCategoriesWithStatsByDateUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryWithStats>> =
        categoryRepository.getCategoriesWithStats(
            startDate = startDate,
            endDate = endDate
        )
}
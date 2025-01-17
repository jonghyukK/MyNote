package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 16..
 * Description:
 */
class GetCategoriesWithNoteCountsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<ApiResult<List<CategoryWithPurchaseNoteCount>>> =
        categoryRepository.getCategoriesWithNoteCounts(startDate, endDate)
}
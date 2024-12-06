package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.safeApiCall
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 6..
 * Description:
 */

class GetCategoryByNameUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryName: String): Flow<ApiResult<Category?>> =
        safeApiCall { categoryRepository.getCategoryByName(categoryName) }
}
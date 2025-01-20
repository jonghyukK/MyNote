package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 9..
 * Description:
 *
 *  카테고리 삭제 UseCase
 *
 */
class DeleteCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Int): Flow<ApiResult<Unit>> =
        categoryRepository.deleteCategoryById(categoryId)
}
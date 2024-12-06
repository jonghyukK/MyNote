package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.getResult
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class MakeCategoryUseCase @Inject constructor(
    private val getCategoryByNameUseCase: GetCategoryByNameUseCase,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Flow<ApiResult<Long>> =
        getCategoryByNameUseCase(category.categoryName).map { categoryResult ->
            categoryResult.getResult(
                loading = {
                    ApiResult.Loading
                },
                error = {
                    ApiResult.Error(it.error)
                },
                success = {
                    val existingCategory = it.data
                    if (existingCategory != null) {
                        ApiResult.Error(MakeCategoryException("이미 해당 카테고리가 존재합니다."))
                    } else {
                        try {
                            val newCategoryId = categoryRepository.insertCategory(category)
                            ApiResult.Success(newCategoryId)
                        } catch (e: Exception) {
                            ApiResult.Error(e)
                        }
                    }
                }
            )
        }

    class MakeCategoryException(msg: String): Exception(msg)
}
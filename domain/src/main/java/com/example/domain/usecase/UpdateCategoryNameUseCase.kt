package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.getResult
import com.example.domain.model.onError
import com.example.domain.model.onSuccess
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 9..
 * Description:
 */
class UpdateCategoryNameUseCase @Inject constructor(
    private val getCategoryByNameUseCase: GetCategoryByNameUseCase,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Flow<ApiResult<Unit>> =
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
                    if (existingCategory == null) {
                        try {
                            categoryRepository.updateCategoryName(category)
                            ApiResult.Success(Unit)
                        } catch (e: Exception) {
                            ApiResult.Error(e)
                        }
                    } else if (existingCategory.id != category.id) {
                        ApiResult.Error(UpdateCategoryException("이미 해당 카테고리가 존재합니다."))
                    } else {
                        ApiResult.Error(UpdateCategoryException("카테고리명을 변경해주세요."))
                    }
                }
            )
        }

    class UpdateCategoryException(msg: String): Exception(msg)
}
package com.example.domain.usecase

import com.example.domain.model.Category
import com.example.domain.model.Result
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class MakeCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Flow<Result<Long>> = flow {
        if (category.categoryName.isBlank()) {
            emit(Result.Error("카테고리명을 입력해주세요."))
            return@flow
        }

        emit(Result.Loading)

        try {
            val isExistCategory = categoryRepository.getCategoryByName(category.categoryName)
            if (isExistCategory == null) {
                val newCategoryId = categoryRepository.insertCategory(category)
                emit(Result.Success(newCategoryId))
            } else {
                emit(Result.Error("이미 해당 카테고리가 존재합니다."))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
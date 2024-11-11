package com.example.domain.usecase

import com.example.domain.model.Category
import com.example.domain.model.Result
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.processNextEventInCurrentThread
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 9..
 * Description:
 */
class UpdateCategoryNameUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(category: Category): Flow<Result<Unit>> = flow {
        if (category.categoryName.isBlank()) {
            emit(Result.Error("카테고리명을 입력해주세요"))
            return@flow
        }

        emit(Result.Loading)

        try {
            categoryRepository.updateCategoryName(category)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
package com.example.domain.usecase

import com.example.domain.model.Result
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 9..
 * Description:
 */
class DeleteCategoryByIdUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(categoryId: Int): Flow<Result<Unit>> = flow {
        emit(Result.Loading)

        try {
            categoryRepository.deleteCategoryById(categoryId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message))
        }
    }
}
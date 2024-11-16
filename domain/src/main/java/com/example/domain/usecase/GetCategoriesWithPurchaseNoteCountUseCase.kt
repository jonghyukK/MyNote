package com.example.domain.usecase

import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 16..
 * Description:
 */
class GetCategoriesWithPurchaseNoteCountUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategoryWithPurchaseNoteCount>> =
        categoryRepository.getCategoriesWithPurchaseNoteCount()
}
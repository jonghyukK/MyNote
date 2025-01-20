package com.kjh.data.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.Category
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.model.asResult
import com.example.domain.model.safeApiCall
import com.example.domain.repository.CategoryRepository
import com.kjh.data.model.entity.toDomainModel
import com.kjh.data.model.entity.toEntity
import com.kjh.data.source.local.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */
class CategoryRepositoryImpl @Inject constructor(
    private val categoryLocalDataSource: CategoryDao
): CategoryRepository {

    override fun allCategoriesFlow(): Flow<ApiResult<List<Category>>> =
        categoryLocalDataSource.getAllCategories()
            .map { it.toDomainModel() }
            .asResult()

    override fun getCategoriesWithNoteCounts(
        startDate: Long?,
        endDate: Long?
    ): Flow<ApiResult<List<CategoryWithPurchaseNoteCount>>> =
        categoryLocalDataSource.getCategoriesWithPurchaseNoteCount(startDate, endDate)
            .asResult()

    override suspend fun upsertCategory(category: Category): Flow<ApiResult<Long>> =
        safeApiCall {
            checkDuplicate(category)

            categoryLocalDataSource.upsert(category.toEntity())
        }

    override suspend fun deleteCategoryById(id: Int): Flow<ApiResult<Unit>> =
        safeApiCall {
            categoryLocalDataSource.deleteCategoryById(id)
        }

    override suspend fun getCategoryByName(name: String): Category? {
        return categoryLocalDataSource.getCategoryByName(name)?.toDomainModel()
    }

    override suspend fun getCategoryById(id: Int): Category? {
        return categoryLocalDataSource.getCategoryById(id)?.toDomainModel()
    }

    private suspend fun checkDuplicate(category: Category) {
        val categoryByName = categoryLocalDataSource.getCategoryByName(category.categoryName)
        if (categoryByName != null && categoryByName.id != category.id) {
            throw Exception(ERROR_DUPLICATE_CATEGORY_NAME)
        }
    }

    companion object {
        private const val ERROR_DUPLICATE_CATEGORY_NAME = "같은 이름을 가진 카테고리가 존재합니다."
    }
}
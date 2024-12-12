package com.kjh.data.repository

import com.example.domain.model.Category
import com.example.domain.model.CategoryWithPurchaseNoteCount
import com.example.domain.model.CategoryWithPurchaseNotesCountAndTotalPrice
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

    override fun allCategoriesFlow(): Flow<List<Category>> =
        categoryLocalDataSource.getAllCategories().map { it.toDomainModel() }

    override fun getCategoriesWithPurchaseNoteCount(): Flow<List<CategoryWithPurchaseNoteCount>> {
        return categoryLocalDataSource.getCategoriesWithPurchaseNoteCount()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryLocalDataSource.insert(category.toEntity())
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categoryLocalDataSource.getCategoryByName(name)?.toDomainModel()
    }

    override suspend fun getCategoryById(id: Int): Category? {
        return categoryLocalDataSource.getCategoryById(id)?.toDomainModel()
    }

    override suspend fun updateCategoryName(category: Category) {
        categoryLocalDataSource.updateCategoryName(category.id, category.categoryName)
    }

    override suspend fun deleteCategoryById(id: Int) {
        categoryLocalDataSource.deleteCategoryById(id)
    }

    override fun getCategoriesWithPurchaseNotesCountAndTotalPrice(): Flow<List<CategoryWithPurchaseNotesCountAndTotalPrice>> {
        return categoryLocalDataSource.getCategoriesWithPurchaseNotesCountAndTotalPrice()
    }
}
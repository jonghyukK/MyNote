package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.Category
import com.example.domain.model.CategoryWithPurchaseNoteCount
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 14..
 * Description:
 */

@Parcelize
data class CategoryUiModel(
    val id: Int,
    val categoryName: String,
    val purchaseNoteCount: Int = 0
): Parcelable {
    fun isDefaultCategory() = id == 999
}

/**
 *  Category (domain) -> CategoryUiModel (presentation)
 */
fun Category.toUiModel() = CategoryUiModel(
    id = id,
    categoryName = categoryName
)

/**
 *  List<Category> (domain) -> List<CategoryUiModel> (presentation)
 */
fun List<Category>.toUiModel() = map(Category::toUiModel)

/**
 *  CategoryUiModel (presentation) -> Category (domain)
 */
fun CategoryUiModel.toDomainModel() = Category(
    id = id,
    categoryName = categoryName
)

/**
 *  CategoryWithPurchaseNoteCount (domain) -> CategoryUiModel (presentation)
 */
fun CategoryWithPurchaseNoteCount.toUiModel() = CategoryUiModel(
    id = categoryId,
    categoryName = categoryName,
    purchaseNoteCount = purchaseNoteCount
)
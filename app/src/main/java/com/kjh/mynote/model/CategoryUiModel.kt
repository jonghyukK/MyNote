package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.Category
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 14..
 * Description:
 */

@Parcelize
data class CategoryUiModel(
    val id: Int,
    val categoryName: String
): Parcelable

/**
 *  Category (domain) -> CategoryUiModel (presentation)
 */
fun Category.toUiModel() = CategoryUiModel(
    id = id,
    categoryName = categoryName
)

/**
 *  CategoryUiModel (presentation) -> Category (domain)
 */
fun CategoryUiModel.toDomainModel() = Category(
    id = id,
    categoryName = categoryName
)
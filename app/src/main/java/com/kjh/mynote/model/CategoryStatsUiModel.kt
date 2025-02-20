package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.CategoryStats
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 19..
 * Description:
 */

@Parcelize
data class CategoryStatsUiModel(
    val categoryId: Int,
    val categoryName: String,
    val purchaseNoteTotalCount: Int,
    val purchaseNoteTotalPrice: Long
): Parcelable

fun CategoryStats.toUiModel() =
    CategoryStatsUiModel(
        categoryId = categoryId,
        categoryName = categoryName,
        purchaseNoteTotalCount = purchaseNoteTotalCount,
        purchaseNoteTotalPrice = purchaseNoteTotalPrice
    )

fun List<CategoryStats>.toUiModel() = map(CategoryStats::toUiModel)
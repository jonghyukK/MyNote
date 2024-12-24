package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
data class CategoryStats(
    val categoryId: Int,
    val categoryName: String,
    val purchaseNoteTotalCount: Int,
    val purchaseNoteTotalPrice: Long
)
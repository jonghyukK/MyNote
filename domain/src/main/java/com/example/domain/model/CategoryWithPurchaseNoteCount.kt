package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 16..
 * Description:
 */
data class CategoryWithPurchaseNoteCount(
    val categoryId: Int,
    val categoryName: String,
    val purchaseNoteCount: Int
)
package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
data class CategoryWithPurchaseNotesCountAndTotalPrice(
    val categoryId: Int,
    val categoryName: String,
    val purchaseNoteCount: Int,
    val totalPurchasePrice: Long
)
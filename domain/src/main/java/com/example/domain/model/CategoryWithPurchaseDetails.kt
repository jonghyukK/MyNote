package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */
data class CategoryWithPurchaseDetails(
    val categoryId: Int,
    val categoryName: String,
    val purchaseNoteCount: Int,
    val purchaseDetails: List<PurchaseDetail>
)

data class PurchaseDetail(
    val purchaseName: String,
    val count: Int
)
package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 18..
 * Description:
 */

data class CategoryPurchaseNoteStats(
    val categoryTotalCount: Int = 0,
    val categoryTotalPrice: Long = 0,
    val purchaseNameStats: List<PurchaseNameStats>
)
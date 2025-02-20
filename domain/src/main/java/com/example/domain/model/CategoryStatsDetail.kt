package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */

data class CategoryStatsDetail(
    val categoryStats: CategoryStats?,
    val purchaseNameStatsList: List<PurchaseNameStats>
)
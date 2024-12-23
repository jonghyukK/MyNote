package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */
data class PurchaseNoteStatistics(
    val totalNoteCount: Int,
    val totalPurchasePrice: Long,
    val categoryStatsList: List<CategoryStats>
)
package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 20..
 * Description:
 */
data class PurchaseNoteStatistics(
    val totalNoteCount: Int,
    val totalPurchasePrice: Long,
    val weeklyStatsList: List<WeeklyPurchaseNoteStatistics>,
    val categoryStatsList: List<CategoryStats>,
    val paymentMethodStatsList: List<PaymentMethodStats>
)
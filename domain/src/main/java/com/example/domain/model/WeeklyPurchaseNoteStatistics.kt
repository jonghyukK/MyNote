package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
data class WeeklyPurchaseNoteStatistics(
    val monthlyWeekInfo: MonthlyWeekInfo,
    val weeklyTotalPrice: Long,
    val weeklyTotalCount: Int
)
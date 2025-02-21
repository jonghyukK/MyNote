package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
data class MonthlyWeekInfo(
    val month: Int,
    val week: Int,
    val weekStartDate: Long,
    val weekEndDate: Long
)
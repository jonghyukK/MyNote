package com.kjh.mynote.model

import com.example.domain.model.WeeklyPurchaseNoteStatistics
import com.kjh.mynote.utils.extensions.toStringWithFormat

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
data class WeeklyPurchaseNoteStatsUiModel(
    val month: Int,
    val week: Int,
    val weekStartDate: Long,
    val weekEndDate: Long,
    val totalPrice: Long,
    val totalCount: Int
) {
    val weekLabel = "${month}월 ${week}주차"
    val dateFormatted = "(${weekStartDate.toStringWithFormat("MM.dd")} ~ ${weekEndDate.toStringWithFormat("MM.dd")})"
}

fun WeeklyPurchaseNoteStatistics.toUiModel() =
    WeeklyPurchaseNoteStatsUiModel(
        month = monthlyWeekInfo.month,
        week = monthlyWeekInfo.week,
        weekStartDate = monthlyWeekInfo.weekStartDate,
        weekEndDate = monthlyWeekInfo.weekEndDate,
        totalPrice = weeklyTotalPrice,
        totalCount = weeklyTotalCount
    )

fun List<WeeklyPurchaseNoteStatistics>.toUiModel() = map(WeeklyPurchaseNoteStatistics::toUiModel)


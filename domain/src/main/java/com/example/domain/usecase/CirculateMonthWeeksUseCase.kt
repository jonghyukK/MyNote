package com.example.domain.usecase

import com.example.domain.model.MonthlyWeekInfo
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description: 해당 월의 각 주차별 주 차, 주의 시작 날짜, 주의 끝 날짜 목록을 계산하는 UseCase.
 */
class CirculateMonthWeeksUseCase @Inject constructor() {

    operator fun invoke(monthDateMillis: Long): List<MonthlyWeekInfo> {
        val monthDate = monthDateMillis.toLocalDate()
        val firstDay = monthDate.withDayOfMonth(1)
        val lastDay = monthDate.withDayOfMonth(monthDate.lengthOfMonth())

        val weekStartDay = DayOfWeek.SUNDAY
        val firstWeekStart = firstDay.with(TemporalAdjusters.previousOrSame(weekStartDay))

        val weeks = mutableListOf<MonthlyWeekInfo>()
        var currentWeekStart = firstWeekStart
        var weekCount = 1

        while (currentWeekStart <= lastDay) {
            val weekStart = if (currentWeekStart < firstDay) firstDay else currentWeekStart
            val currentWeekEnd = currentWeekStart.plusDays(6)
            val weekEnd = if (currentWeekEnd > lastDay) lastDay else currentWeekEnd

            weeks.add(
                MonthlyWeekInfo(
                    month = firstDay.monthValue,
                    week = weekCount,
                    weekStartDate = weekStart.toMillis(),
                    weekEndDate = weekEnd.toMillis()
                )
            )
            weekCount++
            currentWeekStart = currentWeekStart.plusWeeks(1)
        }

        return weeks
    }
}

private fun LocalDate.toMillis(): Long =
    this.atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}
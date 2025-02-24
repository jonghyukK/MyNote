package com.kjh.mynote.utils.extensions

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 23..
 * Description:
 */

/**
 *  LocalDate -> Long 으로 변환.
 *
 * @return Long
 */
fun LocalDate.toMillis(): Long {
    return this
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

/**
 * LocalDate 해당 달의 첫째 날 구하기
 *
 * @return LocalDate
 */
fun LocalDate.getFirstDayOfMonth(): LocalDate {
    return this.withDayOfMonth(1)
}

/**
 * LocalDate 해당 달의 마지막 날 구하기
 *
 * @return LocalDate
 */
fun LocalDate.getLastDayOfMonth(): LocalDate {
    return YearMonth.from(this).atEndOfMonth()
}

/**
 * LocalDate를 특정 날짜 포맷으로 변경
 *
 * @param pattern
 * @return String
 */
fun LocalDate.toStringWithPattern(
    pattern: String
): String {
    val formater = DateTimeFormatter.ofPattern(pattern)
    return this.format(formater)
}

/**
 * YearMonth를 특정 날짜 포맷으로 변경
 *
 * @param pattern
 * @return String
 */
fun YearMonth.toStringWithPattern(
    pattern: String
): String {
    val formater = DateTimeFormatter.ofPattern(pattern)
    return this.format(formater)
}

/**
 * LocalDate의 주(week)에서 Monday, Sunday 날짜 구하기
 *
 * @return Pair<LocalDate, LocalDate>
 */
fun LocalDate.getWeekStartAndEndDates(): Pair<LocalDate, LocalDate> {
    val startOfWeek = this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = this.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    return startOfWeek to endOfWeek
}

fun LocalDate.isThisYear(): Boolean =
    this.year == LocalDate.now().year

fun LocalDate.isThisMonth(): Boolean {
    val now = LocalDate.now()
    return this.year == now.year && this.month == now.month
}
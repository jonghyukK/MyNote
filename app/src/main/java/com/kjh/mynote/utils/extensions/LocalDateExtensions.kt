package com.kjh.mynote.utils.extensions

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 23..
 * Description:
 */

/**
 *  LocalDate -> Long 으로 변환.
 *
 * @return
 */
fun LocalDate.toMillis(): Long {
    return this
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

fun LocalDate.getFirstDayOfMonth(): LocalDate {
    return this.withDayOfMonth(1)
}

fun LocalDate.getLastDayOfMonth(): LocalDate {
    return YearMonth.from(this).atEndOfMonth()
}

fun LocalDate.toStringWithPattern(
    pattern: String
): String {
    val formater = DateTimeFormatter.ofPattern(pattern)
    return this.format(formater)
}

fun YearMonth.toStringWithPattern(
    pattern: String
): String {
    val formater = DateTimeFormatter.ofPattern(pattern)
    return this.format(formater)
}
package com.kjh.mynote.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 9..
 * Description:
 */


object CalendarUtils {

    fun convertLongToDateFormat(
        timeInMillis: Long,
        format: String = "yyyy-MM-dd"
    ): String {
        val date = Date(timeInMillis)
        val dateFormat = SimpleDateFormat(format, Locale.KOREAN)

        return dateFormat.format(date)
    }

    fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대 사용
            .toLocalDate() // LocalDate로 변환
    }

    fun localDateToMillis(localDate: LocalDate): Long {
        return localDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun getStartAndEndOfMonth(year: Int, month: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, month, 1) // 해당 월의 첫 번째 날
        val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth()) // 해당 월의 마지막 날

        val startMillis = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return Pair(startMillis, endMillis)
    }

    fun localDateToStringWithPattern(
        localDate: LocalDate,
        pattern: String
    ): String {
        val formater = DateTimeFormatter.ofPattern(pattern)
        return localDate.format(formater)
    }

    fun yearMonthToStringWithPattern(
        yearMonth: YearMonth,
        pattern: String
    ): String {
        val formater = DateTimeFormatter.ofPattern(pattern)
        return yearMonth.format(formater)
    }
}
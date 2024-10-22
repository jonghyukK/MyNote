package com.kjh.mynote.utils

import java.time.LocalDate
import java.time.ZoneId

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 9..
 * Description:
 */


object CalendarUtils {

    fun getStartAndEndOfMonth(year: Int, month: Int): Pair<Long, Long> {
        val startOfMonth = LocalDate.of(year, month, 1) // 해당 월의 첫 번째 날
        val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth()) // 해당 월의 마지막 날

        val startMillis = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return Pair(startMillis, endMillis)
    }
}
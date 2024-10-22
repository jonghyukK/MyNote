package com.kjh.mynote.utils.extensions

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 23..
 * Description:
 */


fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대 사용
        .toLocalDate() // LocalDate로 변환
}

fun Long.toStringWithFormat(
    format: String = "yyyy-MM-dd"
): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(format, Locale.KOREAN)

    return dateFormat.format(date)
}
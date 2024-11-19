package com.kjh.mynote.utils.extensions

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 23..
 * Description:
 */


fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}

fun Long.toStringWithFormat(
    format: String = "yyyy-MM-dd"
): String {
    val date = Date(this)
    val dateFormat = SimpleDateFormat(format, Locale.KOREAN)

    return dateFormat.format(date)
}

fun Long.toComma(): String {
    val decimal = DecimalFormat("#,###")
    return decimal.format(this)
}
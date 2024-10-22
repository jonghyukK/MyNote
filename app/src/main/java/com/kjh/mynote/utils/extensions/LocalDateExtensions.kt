package com.kjh.mynote.utils.extensions

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 23..
 * Description:
 */

fun LocalDate.toMillis(): Long {
    return this
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun LocalDate.toStringWithPattern(
    pattern: String
): String {
    val formater = DateTimeFormatter.ofPattern(pattern)
    return this.format(formater)
}
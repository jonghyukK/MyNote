package com.kjh.mynote.utils

import java.text.SimpleDateFormat
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
}
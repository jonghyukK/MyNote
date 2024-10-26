package com.kjh.data.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 26..
 * Description:
 */

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대 사용
        .toLocalDate() // LocalDate로 변환
}
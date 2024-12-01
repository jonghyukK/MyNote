package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 2..
 * Description:
 */

enum class SortType(val title: String) {
    LATEST("최근 날짜 순"),
    OLDEST("오래된 날짜 순"),
    HIGH_PRICE("가격 높은 순"),
    LOW_PRICE("가격 낮은 순")
}
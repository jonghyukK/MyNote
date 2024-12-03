package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 2..
 * Description:
 */
data class PurchaseNoteSearchFilterInfo(
    val categories: List<Category>,
    val maxPrice: Long? = null
)
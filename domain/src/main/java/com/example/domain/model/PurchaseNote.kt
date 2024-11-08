package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

data class PurchaseNote(
    val id: Int = 0,
    val purchaseDate: Long,
    val purchasePrice: Long,
    val category: String,
    val images: List<String>? = null,
    val purchasePlaceInfo: PurchasePlaceInfo? = null
)
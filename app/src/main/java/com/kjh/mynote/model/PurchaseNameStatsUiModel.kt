package com.kjh.mynote.model

import com.example.domain.model.PurchaseNameStats

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */
data class PurchaseNameStatsUiModel(
    val purchaseName: String,
    val totalCount: Int,
    val totalPrice: Long
)

fun PurchaseNameStats.toUiModel() = PurchaseNameStatsUiModel(
    purchaseName = purchaseName,
    totalCount = totalCount,
    totalPrice = totalPrice
)

fun List<PurchaseNameStats>.toUiModel() = map(PurchaseNameStats::toUiModel)
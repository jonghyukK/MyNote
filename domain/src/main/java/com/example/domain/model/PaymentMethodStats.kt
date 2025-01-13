package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 12..
 * Description:
 */
data class PaymentMethodStats(
    val paymentMethodId: Int,
    val paymentMethodName: String,
    val purchaseNoteTotalCount: Int,
    val purchaseNoteTotalPrice: Long
)
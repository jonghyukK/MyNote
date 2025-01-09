package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 9..
 * Description:
 */

data class PurchaseNoteFilters(
    val categories: List<Category>,
    val paymentMethods: List<PaymentMethod>,
    val highestPrice: Long?
)
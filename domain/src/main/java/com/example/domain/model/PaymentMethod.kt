package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

data class PaymentMethod(
    val paymentMethodId: Int = 0,
    val paymentMethodName: String,
    val isDefault: Boolean = false
)
package com.kjh.mynote.model

import com.example.domain.model.PaymentMethod

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
data class PaymentMethodUiModel(
    val paymentMethodId: Int,
    val paymentMethodName: String
) {
    fun isDefaultMethod() = paymentMethodId == 999
}

/**
 * PaymentMethod (domain) -> PaymentMethodUiModel (presentation)
 */
fun PaymentMethod.toUiModel() = PaymentMethodUiModel(
    paymentMethodId = paymentMethodId,
    paymentMethodName = paymentMethodName
)

/**
 *  List<PaymentMethod> (domain) -> List<PaymentMethodUiModel> (presentation)
 */
fun List<PaymentMethod>.toUiModel() = map(PaymentMethod::toUiModel)

/**
 *  PaymentMethodUiModel (presentation) -> PaymentMethod (domain)
 */
fun PaymentMethodUiModel.toDomainModal() = PaymentMethod(
    paymentMethodId = paymentMethodId,
    paymentMethodName = paymentMethodName
)
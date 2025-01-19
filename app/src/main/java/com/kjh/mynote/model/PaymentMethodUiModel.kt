package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PaymentMethod
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@Parcelize
data class PaymentMethodUiModel(
    val paymentMethodId: Int = 0,
    val paymentMethodName: String = "",
    val isDefault: Boolean = false
): Parcelable {
    fun isDefaultMethod() = paymentMethodId == 999
}

/**
 * PaymentMethod (domain) -> PaymentMethodUiModel (presentation)
 */
fun PaymentMethod.toUiModel() = PaymentMethodUiModel(
    paymentMethodId = paymentMethodId,
    paymentMethodName = paymentMethodName,
    isDefault = isDefault
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
    paymentMethodName = paymentMethodName,
    isDefault = isDefault
)
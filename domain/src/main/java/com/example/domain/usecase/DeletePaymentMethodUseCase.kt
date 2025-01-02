package com.example.domain.usecase

import com.example.domain.repository.PaymentMethodRepository
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
class DeletePaymentMethodUseCase @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(paymentMethodId: Int) =
        paymentMethodRepository.deletePaymentMethod(paymentMethodId)
}
package com.example.domain.usecase

import com.example.domain.model.PaymentMethod
import com.example.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
class GetPaymentMethodsUseCase @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) {
    operator fun invoke(): Flow<List<PaymentMethod>> =
        paymentMethodRepository.getAllPaymentMethods()
}
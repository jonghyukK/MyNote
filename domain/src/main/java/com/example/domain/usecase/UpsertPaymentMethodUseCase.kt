package com.example.domain.usecase

import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.model.safeApiCall
import com.example.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 20..
 * Description:
 */
class UpsertPaymentMethodUseCase @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) {
    suspend operator fun invoke(paymentMethod: PaymentMethod): Flow<ApiResult<Long>> =
        safeApiCall {
            paymentMethodRepository.upsertPaymentMethod(paymentMethod)
        }

}
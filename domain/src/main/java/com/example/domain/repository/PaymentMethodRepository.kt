package com.example.domain.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
interface PaymentMethodRepository {

    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>

    suspend fun makePaymentMethod(paymentMethod: PaymentMethod): Flow<ApiResult<Long>>

    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod): Flow<ApiResult<Unit>>

    suspend fun deletePaymentMethod(paymentMethodId: Int)
}
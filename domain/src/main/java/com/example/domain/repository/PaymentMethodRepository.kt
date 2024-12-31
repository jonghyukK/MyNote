package com.example.domain.repository

import com.example.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
interface PaymentMethodRepository {

    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>

    suspend fun makePaymentMethod(paymentMethod: PaymentMethod): Long

    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod)

    suspend fun deletePaymentMethod(paymentMethodId: Int)
}
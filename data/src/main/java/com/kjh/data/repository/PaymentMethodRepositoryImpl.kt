package com.kjh.data.repository

import com.example.domain.model.PaymentMethod
import com.example.domain.repository.PaymentMethodRepository
import com.kjh.data.model.entity.toDomainModel
import com.kjh.data.model.entity.toEntity
import com.kjh.data.source.local.PaymentMethodDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */
class PaymentMethodRepositoryImpl @Inject constructor(
    private val paymentMethodLocalDataSource: PaymentMethodDao
): PaymentMethodRepository {

    override fun getAllPaymentMethods(): Flow<List<PaymentMethod>> =
        paymentMethodLocalDataSource.getAllPaymentMethods()
            .map { it.toDomainModel() }

    override suspend fun makePaymentMethod(paymentMethod: PaymentMethod): Long {
        return paymentMethodLocalDataSource.insert(paymentMethod.toEntity())
    }

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        return paymentMethodLocalDataSource.updatePaymentMethod(
            id = paymentMethod.paymentMethodId,
            newPaymentMethodName = paymentMethod.paymentMethodName
        )
    }

    override suspend fun deletePaymentMethod(paymentMethodId: Int) {
        return paymentMethodLocalDataSource.deletePaymentMethod(paymentMethodId)
    }
}
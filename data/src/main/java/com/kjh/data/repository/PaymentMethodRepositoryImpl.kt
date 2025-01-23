package com.kjh.data.repository

import com.example.domain.model.ApiResult
import com.example.domain.model.PaymentMethod
import com.example.domain.model.asResult
import com.example.domain.model.safeApiCall
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

    override fun getAllPaymentMethods(): Flow<ApiResult<List<PaymentMethod>>> =
        paymentMethodLocalDataSource.getAllPaymentMethods()
            .map { it.toDomainModel() }
            .asResult()

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod): Long {
        checkDuplicate(paymentMethod)

        if (paymentMethod.isDefault) {
            paymentMethodLocalDataSource.resetDefaultPaymentMethods()
        }

        return paymentMethodLocalDataSource.upsertPaymentMethod(paymentMethod.toEntity())
    }

    override suspend fun deletePaymentMethod(paymentMethodId: Int): Flow<ApiResult<Unit>> {
        return safeApiCall {
            paymentMethodLocalDataSource.deletePaymentMethod(paymentMethodId)
        }
    }

    override suspend fun getDefaultPaymentMethod(): PaymentMethod? =
        paymentMethodLocalDataSource.getDefaultPaymentMethod()?.toDomainModel()

    private suspend fun checkDuplicate(paymentMethod: PaymentMethod) {
        val paymentMethodByName =
            paymentMethodLocalDataSource.getPaymentMethodByName(paymentMethod.paymentMethodName)
        if (paymentMethodByName != null &&
            paymentMethodByName.paymentMethodId != paymentMethod.paymentMethodId) {
            throw Exception(ERROR_DUPLICATE_PAYMENT_METHOD_NAME)
        }
    }

    companion object {
        private const val ERROR_DUPLICATE_PAYMENT_METHOD_NAME = "같음 이름을 가진 결제수단이 존재합니다."
    }
}
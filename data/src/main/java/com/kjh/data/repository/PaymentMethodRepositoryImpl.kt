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

    override suspend fun makePaymentMethod(paymentMethod: PaymentMethod): Flow<ApiResult<Long>> {
        return safeApiCall {
            validateUniquePaymentMethodName(paymentMethod.paymentMethodName)

            paymentMethodLocalDataSource.insert(paymentMethod.toEntity())
        }
    }

    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod): Flow<ApiResult<Unit>> {
        return safeApiCall {
            validateUniquePaymentMethodName(paymentMethod.paymentMethodName)

            paymentMethodLocalDataSource.updatePaymentMethod(
                id = paymentMethod.paymentMethodId,
                newPaymentMethodName = paymentMethod.paymentMethodName
            )
        }
    }

    override suspend fun deletePaymentMethod(paymentMethodId: Int): Flow<ApiResult<Unit>> {
        return safeApiCall {
            paymentMethodLocalDataSource.deletePaymentMethod(paymentMethodId)
        }
    }

    private suspend fun validateUniquePaymentMethodName(name: String) {
        val paymentMethodByName = paymentMethodLocalDataSource.getPaymentMethodByName(name)
        if (paymentMethodByName != null) {
            throw Exception("같은 이름을 가진 결제수단이 존재합니다.")
        }
    }
}
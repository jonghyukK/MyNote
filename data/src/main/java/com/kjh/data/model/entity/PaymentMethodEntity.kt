package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.PaymentMethod

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

@Entity(tableName = "paymentMethod")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val paymentMethodId: Int = 0,
    val paymentMethodName: String
)

fun PaymentMethodEntity.toDomainModel() = PaymentMethod(
    paymentMethodId = paymentMethodId,
    paymentMethodName = paymentMethodName
)

fun List<PaymentMethodEntity>.toDomainModel() = map(PaymentMethodEntity::toDomainModel)

fun PaymentMethod.toEntity() = PaymentMethodEntity(
    paymentMethodId = paymentMethodId,
    paymentMethodName = paymentMethodName
)
package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PaymentMethodStats
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */

@Parcelize
data class PaymentMethodStatsUiModel(
    val paymentMethodId: Int,
    val paymentMethodName: String,
    val purchaseNoteTotalCount: Int,
    val purchaseNoteTotalPrice: Long
): Parcelable

fun PaymentMethodStats.toUiModel() =
    PaymentMethodStatsUiModel(
        paymentMethodId = paymentMethodId,
        paymentMethodName = paymentMethodName,
        purchaseNoteTotalCount = purchaseNoteTotalCount,
        purchaseNoteTotalPrice = purchaseNoteTotalPrice
    )

fun List<PaymentMethodStats>.toUiModel() = map(PaymentMethodStats::toUiModel)
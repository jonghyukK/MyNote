package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.addedit.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 7..
 * Description:
 */

@Serializable data class PaymentMethodAddEditRoute(val paymentMethodId: Int)

fun NavController.navigateToPaymentMethodAddEdit(
    paymentMethodId: Int,
    navOptions: NavOptions? = null
) = navigate(route = PaymentMethodAddEditRoute(paymentMethodId), navOptions = navOptions)
package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 7..
 * Description:
 */


@Serializable data object PaymentMethodManageRoute

fun NavController.navigateToPaymentMethodManage(
    navOptions: NavOptions? = null
) = navigate(route = PaymentMethodManageRoute, navOptions = navOptions)
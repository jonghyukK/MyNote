package com.kjh.mynote.ui_compose.feature.mypage

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.kjh.mynote.ui_compose.feature.mypage.manage.category.CategoryManageRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.PaymentMethodManageRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.add.PaymentMethodAddEditRoute
import kotlinx.serialization.Serializable

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 7..
 * Description:
 */


@Serializable data object MyPageRoute

fun NavController.navigateToMyPage(
    navOptions: NavOptions? = null
) = navigate(route = MyPageRoute, navOptions = navOptions)

fun NavGraphBuilder.myPageNavGraph(
    navigateToCategoryManage: () -> Unit,
    navigateToPaymentMethodManage: () -> Unit,
    navigateToPaymentMethodAddEdit: (Int) -> Unit,
    navigateUp: () -> Unit
) {
    composable<MyPageRoute> {
        MyPageScreen(
            onCategoryManageClick = navigateToCategoryManage,
            onPaymentMethodManageClick = navigateToPaymentMethodManage
        )
    }

    composable<CategoryManageRoute> {
        CategoryManageRoute(
            onBackClick = navigateUp
        )
    }

    composable<PaymentMethodManageRoute> {
        PaymentMethodManageRoute(
            navigateUp = navigateUp,
             navigateToPaymentMethodAddEdit = navigateToPaymentMethodAddEdit
        )
    }

    composable<PaymentMethodAddEditRoute> {
        PaymentMethodAddEditRoute(
            onNavigateUp = navigateUp
        )
    }
}


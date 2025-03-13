package com.kjh.mynote.ui_compose.feature.mypage.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.kjh.mynote.ui_compose.feature.mypage.MyPageScreen
import com.kjh.mynote.ui_compose.feature.mypage.manage.category.CategoryManageRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.category.navigation.CategoryManageRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.PaymentMethodManageRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.addedit.PaymentMethodAddEditRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.addedit.navigation.PaymentMethodAddEditRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.navigation.PaymentMethodManageRoute
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
            modifier = Modifier.fillMaxSize(),
            navigateToCategoryManage = navigateToCategoryManage,
            navigateToPaymentMethodManage = navigateToPaymentMethodManage
        )
    }

    composable<CategoryManageRoute>(
    ) {
        CategoryManageRoute(
            modifier = Modifier.fillMaxSize(),
            navigateUp = navigateUp
        )
    }

    composable<PaymentMethodManageRoute>(
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left, // 오른쪽에서 등장
                animationSpec = tween(300)
            )
        },
        exitTransition = { null },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right, // 뒤로 가기 시 오른쪽으로 사라짐
                animationSpec = tween(300)
            )
        }
    ) {
        PaymentMethodManageRoute(
            modifier = Modifier.fillMaxSize(),
            navigateUp = navigateUp,
            navigateToPaymentMethodAddEdit = navigateToPaymentMethodAddEdit
        )
    }

    composable<PaymentMethodAddEditRoute>(
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        PaymentMethodAddEditRoute(
            modifier = Modifier.fillMaxSize(),
            onNavigateUp = navigateUp
        )
    }
}


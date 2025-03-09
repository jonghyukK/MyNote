package com.kjh.mynote.ui_compose.main.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.kjh.mynote.ui_compose.feature.home.HomeRoute
import com.kjh.mynote.ui_compose.feature.home.homeNavGraph
import com.kjh.mynote.ui_compose.feature.mypage.manage.category.navigateToCategoryManage
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.add.navigateToPaymentMethodAddEdit
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.navigateToPaymentMethodManage
import com.kjh.mynote.ui_compose.feature.mypage.myPageNavGraph
import com.kjh.mynote.ui_compose.feature.placenote.placeNoteNavGraph
import com.kjh.mynote.ui_compose.feature.purchasenote.purchaseNoteNavGraph
import com.kjh.mynote.ui_compose.main.ui.MyNoteAppState

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 9..
 * Description:
 */

@Composable
fun MyNoteAppNavHost(
    myNoteAppState: MyNoteAppState,
    modifier: Modifier = Modifier
) {
    val navController = myNoteAppState.navController

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        modifier = modifier
    ) {
        homeNavGraph()

        placeNoteNavGraph()

        purchaseNoteNavGraph()

        myPageNavGraph(
            navigateToCategoryManage = navController::navigateToCategoryManage,
            navigateToPaymentMethodManage = navController::navigateToPaymentMethodManage,
            navigateToPaymentMethodAddEdit = navController::navigateToPaymentMethodAddEdit,
            navigateUp = navController::popBackStack
        )
    }
}
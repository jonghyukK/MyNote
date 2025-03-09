package com.kjh.mynote.ui_compose.feature.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 7..
 * Description:
 */

@Serializable
data object HomeRoute

fun NavController.navigateToHome(
    navOptions: NavOptions? = null
) = navigate(route = HomeRoute, navOptions = navOptions)

fun NavGraphBuilder.homeNavGraph() {
    composable<HomeRoute> {
        HomeScreen()
    }
}
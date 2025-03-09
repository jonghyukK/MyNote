package com.kjh.mynote.ui_compose.main.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.kjh.mynote.ui_compose.feature.home.navigateToHome
import com.kjh.mynote.ui_compose.feature.mypage.navigation.navigateToMyPage
import com.kjh.mynote.ui_compose.feature.placenote.navigateToPlaceNote
import com.kjh.mynote.ui_compose.feature.purchasenote.navigateToPurchaseNote
import com.kjh.mynote.ui_compose.main.navigation.TopLevelRoute

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 9..
 * Description:
 */

@Composable
fun rememberMyNoteAppState(
    navController: NavHostController = rememberNavController()
): MyNoteAppState {
    return remember(navController) {
        MyNoteAppState(navController)
    }
}

@Stable
class MyNoteAppState(val navController: NavHostController) {
    private val previousRoute = mutableStateOf<NavDestination?>(null)

    val currentRoute: NavDestination?
        @Composable get() {
            val currentEntry = navController.currentBackStackEntryAsState()

            return currentEntry.value?.destination.also { route ->
                if (route != null) {
                    previousRoute.value = route
                }
            } ?: previousRoute.value
        }

    val currentTopLevelRoute: TopLevelRoute?
        @Composable get() {
            return TopLevelRoute.entries.firstOrNull { topLevelRoute ->
                currentRoute?.hasRoute(route = topLevelRoute.route) == true
            }
        }

    val topLevelRoutes: List<TopLevelRoute> = TopLevelRoute.entries

    fun navigateToTopLevelRoute(topLevelRoute: TopLevelRoute) {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        when (topLevelRoute) {
            TopLevelRoute.HOME -> navController.navigateToHome(topLevelNavOptions)
            TopLevelRoute.PLACE_NOTE -> navController.navigateToPlaceNote(topLevelNavOptions)
            TopLevelRoute.PURCHASE_NOTE -> navController.navigateToPurchaseNote(topLevelNavOptions)
            TopLevelRoute.MY_PAGE -> navController.navigateToMyPage(topLevelNavOptions)
        }
    }
}
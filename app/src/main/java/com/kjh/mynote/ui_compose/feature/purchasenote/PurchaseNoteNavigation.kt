package com.kjh.mynote.ui_compose.feature.purchasenote

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
data object PurchaseNoteRoute

fun NavController.navigateToPurchaseNote(
    navOptions: NavOptions? = null
) = navigate(route = PurchaseNoteRoute, navOptions = navOptions)

fun NavGraphBuilder.purchaseNoteNavGraph() {
    composable<PurchaseNoteRoute> {
        PurchaseNoteScreen()
    }
}
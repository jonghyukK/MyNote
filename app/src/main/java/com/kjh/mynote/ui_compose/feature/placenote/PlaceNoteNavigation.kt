package com.kjh.mynote.ui_compose.feature.placenote

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


@Serializable data object PlaceNoteRoute

fun NavController.navigateToPlaceNote(
    navOptions: NavOptions? = null
) = navigate(route = PlaceNoteRoute, navOptions = navOptions)

fun NavGraphBuilder.placeNoteNavGraph() {
    composable<PlaceNoteRoute> {
        PlaceNoteScreen()
    }
}
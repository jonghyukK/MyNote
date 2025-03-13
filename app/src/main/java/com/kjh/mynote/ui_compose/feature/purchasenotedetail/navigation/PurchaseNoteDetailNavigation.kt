package com.kjh.mynote.ui_compose.feature.purchasenotedetail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 13..
 * Description:
 */


@Serializable data class PurchaseNoteDetailRoute(val purchaseNoteId: Int)

fun NavController.navigateToPurchaseNoteDetail(
    purchaseNoteId: Int,
    navOptions: NavOptions? = null
) = navigate(route = PurchaseNoteDetailRoute(purchaseNoteId), navOptions)
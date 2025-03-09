package com.kjh.mynote.ui_compose.main.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.feature.home.HomeRoute
import com.kjh.mynote.ui_compose.feature.mypage.MyPageRoute
import com.kjh.mynote.ui_compose.feature.placenote.PlaceNoteRoute
import com.kjh.mynote.ui_compose.feature.purchasenote.PurchaseNoteRoute
import kotlin.reflect.KClass

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

enum class TopLevelRoute(
    val icon: ImageVector,
    @StringRes val titleId: Int,
    val route: KClass<*>
) {
    HOME(
        icon = Icons.Default.Home,
        titleId = R.string.bnv_menu_home,
        route = HomeRoute::class
    ),
    PLACE_NOTE(
        icon = Icons.Default.CalendarMonth,
        titleId = R.string.bnv_menu_place_note,
        route = PlaceNoteRoute::class
    ),
    PURCHASE_NOTE(
        icon = Icons.Default.CalendarMonth,
        titleId = R.string.bnv_menu_purchase_note,
        route = PurchaseNoteRoute::class
    ),
    MY_PAGE(
        icon = Icons.Default.Person,
        titleId = R.string.bnv_menu_my_page,
        route = MyPageRoute::class
    )
}
package com.kjh.mynote.ui_compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.kjh.mynote.ui_compose.feature.home.HomeScreen
import com.kjh.mynote.ui_compose.feature.mypage.MyPageScreen
import com.kjh.mynote.ui_compose.feature.placenote.PlaceNoteScreen
import com.kjh.mynote.ui_compose.feature.purchasenote.PurchaseNoteScreen

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */
interface TopLevelDestination {
    val icon: ImageVector
    val route: String
    val screen: @Composable () -> Unit
}

object Home: TopLevelDestination {
    override val icon: ImageVector = Icons.Default.Home
    override val route: String = "홈"
    override val screen: @Composable () -> Unit = { HomeScreen() }
}

object PlaceNote: TopLevelDestination {
    override val icon: ImageVector = Icons.Default.CalendarMonth
    override val route: String = "장소노트"
    override val screen: @Composable () -> Unit = { PlaceNoteScreen() }
}

object PurchaseNote: TopLevelDestination {
    override val icon: ImageVector = Icons.Default.CalendarMonth
    override val route: String = "구매노트"
    override val screen: @Composable () -> Unit = { PurchaseNoteScreen() }
}

object MyPage: TopLevelDestination {
    override val icon: ImageVector = Icons.Default.Person
    override val route: String = "마이페이지"
    override val screen: @Composable () -> Unit = { MyPageScreen() }
}

val topLevelRoutes = listOf(Home, PlaceNote, PurchaseNote, MyPage)
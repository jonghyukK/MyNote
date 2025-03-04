package com.kjh.mynote.ui_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.feature.home.HomeScreen
import com.kjh.mynote.ui_compose.feature.mypage.MyPageScreen
import com.kjh.mynote.ui_compose.feature.mypage.manage.category.CategoryManageRoute
import com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.PaymentMethodManageScreen
import com.kjh.mynote.ui_compose.feature.placenote.PlaceNoteScreen
import com.kjh.mynote.ui_compose.feature.purchasenote.PurchaseNoteScreen
import com.kjh.mynote.ui_compose.theme.MyNoteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@AndroidEntryPoint
class ComposeMainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyNoteTheme {
                MyNoteApp()
            }
        }
    }
}

@Composable
fun MyNoteApp() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutesList = topLevelRoutes.map { it.route }
    val showBottomBar = currentRoute in topLevelRoutesList

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colorResource(R.color.white)
                ) {
                    topLevelRoutes.forEach { topLevelRoute ->
                        NavigationBarItem(
                            icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.route) },
                            label = { Text(topLevelRoute.route) },
                            selected = currentRoute == topLevelRoute.route,
                            onClick = { navController.navigate(topLevelRoute.route) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colorResource(R.color.black_900),
                                selectedTextColor = colorResource(R.color.black_900),
                                unselectedIconColor = colorResource(R.color.black_500),
                                unselectedTextColor = colorResource(R.color.black_500)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Home.route,
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.white))
                .padding(innerPadding)
        ) {
            composable(route = Home.route) {
                HomeScreen()
            }

            composable(route = PlaceNote.route) {
                PlaceNoteScreen()
            }

            composable(route = PurchaseNote.route) {
                PurchaseNoteScreen()
            }

            navigation(
                startDestination = MyPage.route,
                route = "myPage/main"
            ) {
                composable(MyPage.route) {
                    MyPageScreen(
                        onCategoryManageClick = { navController.navigate("myPage/category") },
                        onPaymentMethodManageClick = { navController.navigate("myPage/paymentMethod") }
                    )
                }

                composable("myPage/category") {
                    CategoryManageRoute(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("myPage/paymentMethod") {
                    PaymentMethodManageScreen()
                }
            }
        }
    }
}
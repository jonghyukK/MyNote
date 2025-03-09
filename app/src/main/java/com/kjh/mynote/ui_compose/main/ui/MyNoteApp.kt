package com.kjh.mynote.ui_compose.main.ui

import androidx.annotation.ColorRes
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.main.navigation.MyNoteAppNavHost

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 9..
 * Description:
 */

@Composable
fun MyNoteApp(
    myNoteAppState: MyNoteAppState,
    modifier: Modifier = Modifier
) {
    val currentTopLevelRoute = myNoteAppState.currentTopLevelRoute

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentTopLevelRoute != null) {
                NavigationBar(containerColor = Color.White) {
                    myNoteAppState.topLevelRoutes.forEach { topLevelRoute ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = topLevelRoute.icon,
                                    contentDescription = topLevelRoute.name
                                )
                            },
                            label = { Text(text = stringResource(topLevelRoute.titleId)) },
                            selected = currentTopLevelRoute == topLevelRoute,
                            onClick = { myNoteAppState.navigateToTopLevelRoute(topLevelRoute) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colorResource(id = selectedColor),
                                selectedTextColor = colorResource(id = selectedColor),
                                unselectedIconColor = colorResource(id = unselectedColor),
                                unselectedTextColor = colorResource(id = unselectedColor)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        MyNoteAppNavHost(
            myNoteAppState = myNoteAppState,
            modifier = modifier.padding(innerPadding)
        )
    }
}

@ColorRes
private val selectedColor = R.color.black_900

@ColorRes
private val unselectedColor = R.color.black_500
package com.kjh.mynote.ui_compose.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kjh.mynote.ui_compose.main.ui.MyNoteApp
import com.kjh.mynote.ui_compose.main.ui.rememberMyNoteAppState
import com.kjh.mynote.ui_compose.theme.MyNoteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@AndroidEntryPoint
class ComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appState = rememberMyNoteAppState()

            MyNoteTheme {
                MyNoteApp(
                    myNoteAppState = appState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        }
    }
}
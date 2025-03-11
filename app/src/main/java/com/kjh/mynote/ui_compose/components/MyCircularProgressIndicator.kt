package com.kjh.mynote.ui_compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kjh.mynote.ui_compose.theme.ColorPrimary

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 4..
 * Description:
 */

@Composable
fun MyCircularProgressIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
        ) {
        CircularProgressIndicator(
            modifier = Modifier.size(46.dp),
            color = ColorPrimary
        )
    }
}
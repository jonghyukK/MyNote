package com.kjh.mynote.ui_compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjh.mynote.R

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun MyTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    placeHolder: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = if (isFocused) R.color.colorPrimary else R.color.black_600

    BasicTextField(
        value = value,
        onValueChange = onValueChanged,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = TextStyle(fontSize = 16.sp, color = colorResource(id = R.color.black_900)),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp) // 원하는 높이 지정
            .background(Color.White, shape = RoundedCornerShape(8.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = colorResource(id = borderColor),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp), // 내부 수평 패딩 적용 (세로 패딩은 height에 맞추어 조절)
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeHolder,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.black_600)
                    )
                }
                innerTextField()
            }
        }
    )
}
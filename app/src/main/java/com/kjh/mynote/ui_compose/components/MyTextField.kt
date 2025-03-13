package com.kjh.mynote.ui_compose.components

import androidx.annotation.StringRes
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
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.theme.Black600
import com.kjh.mynote.ui_compose.theme.Black900
import com.kjh.mynote.ui_compose.theme.ColorPrimary
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun MyTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChanged: (String) -> Unit,
    @StringRes placeHolderRes: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) ColorPrimary else Black600

    BasicTextField(
        value = value,
        onValueChange = onValueChanged,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = TextStyle(fontSize = 14.sp, color = Black900),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeHolderRes != null) {
                    Text(
                        text = stringResource(placeHolderRes),
                        fontSize = 14.sp,
                        color = Black600
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MyTextFieldPreview() {
    MyTextField(
        value = "",
        onValueChanged = {},
        placeHolderRes = R.string.input_category_name
    )
}

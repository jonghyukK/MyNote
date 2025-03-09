package com.kjh.mynote.ui_compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.theme.Black500
import com.kjh.mynote.ui_compose.theme.ColorPrimary

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 5..
 * Description:
 */

@Composable
fun MyBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @StringRes btnTextRes: Int,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorPrimary,
                disabledContainerColor = Black500
            ),
            onClick = onClick
        ) {
            Text(
                text = stringResource(btnTextRes),
                fontSize = 17.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyBottomButtonPreview() {
    MyBottomButton(
        onClick = {},
        btnTextRes = R.string.do_register
    )
}
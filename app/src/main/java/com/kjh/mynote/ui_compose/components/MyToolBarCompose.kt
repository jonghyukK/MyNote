package com.kjh.mynote.ui_compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.theme.Black100
import com.kjh.mynote.ui_compose.theme.Black900
import com.kjh.mynote.ui_compose.theme.MyNoteTheme
import com.kjh.mynote.ui_compose.theme.spoqaSansFamily

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 26..
 * Description:
 */
@Composable
fun MyToolBarCompose(
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    onBackButtonClick: () -> Unit = {},
    @StringRes titleRes: Int,
    rightFirstImageRes: Int? = null,
    rightFirstImageDesc: String = "",
    rightFirstImageClick: () -> Unit = {},
    rightSecondImageRes: Int? = null,
    rightSecondImageDesc: String = "",
    rightSecondImageClick: () -> Unit = {}
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp)
                .height(dimensionResource(id = R.dimen.actionbar_size)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                ToolBarIcon(
                    imageRes = R.drawable.ic_chevron_left_24_white,
                    contentDesc = "backButton",
                    clickAction = onBackButtonClick
                )
            }

            Text(
                text = stringResource(titleRes),
                fontSize = 20.sp,
                fontFamily = spoqaSansFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.03).sp,
                color = Black900,
                modifier = Modifier.padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (rightSecondImageRes != null) {
                ToolBarIcon(
                    imageRes = rightSecondImageRes,
                    contentDesc = rightSecondImageDesc,
                    clickAction = rightSecondImageClick
                )
            }

            if (rightFirstImageRes != null) {
                ToolBarIcon(
                    imageRes = rightFirstImageRes,
                    contentDesc = rightFirstImageDesc,
                    clickAction = rightFirstImageClick
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Black100)
    }
}

@Composable
fun ToolBarIcon(
    modifier: Modifier = Modifier,
    imageRes: Int,
    contentDesc: String,
    clickAction: () -> Unit
) {
    IconButton(
        onClick = clickAction,
        modifier = Modifier.size(44.dp)
    ) {
        Icon(
            painter = painterResource(id = imageRes),
            contentDescription = contentDesc,
            tint = Black900,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyToolBarPreview() {
    MyNoteTheme {
        MyToolBarCompose(titleRes = R.string.title_edit_category)
    }
}
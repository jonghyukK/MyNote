package com.kjh.mynote.ui_compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.theme.Black500
import com.kjh.mynote.ui_compose.theme.Black700
import com.kjh.mynote.ui_compose.theme.Black900

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 2..
 * Description:
 */

@Composable
fun MyDefaultDialog(
    @StringRes titleRes: Int,
    @StringRes descRes: Int? = null,
    @StringRes confirmButtonTextRes: Int = R.string.confirm,
    @StringRes cancelButtonTextRes: Int = R.string.cancel,
    descFontColor: Color = Black500,
    onClickConfirm: () -> Unit,
    onClickCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onClickCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Title(titleRes = titleRes)

                if (descRes != null) {
                    Spacer(Modifier.height(20.dp))
                    Description(
                        descRes = descRes,
                        fontColor = descFontColor
                    )
                }

                Spacer(Modifier.height(24.dp))

                PositiveButton(
                    btnTextRes = confirmButtonTextRes,
                    onClick = onClickConfirm
                )

                Spacer(modifier = Modifier.height(6.dp))

                NegativeButton(
                    btnTextRes = cancelButtonTextRes,
                    onClick = onClickCancel
                )
            }
        }
    }
}

@Composable
private fun NegativeButton(
    @StringRes btnTextRes: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color = Black700),
        onClick = onClick
    ) {
        Text(
            text = stringResource(btnTextRes),
            fontSize = 15.sp,
            color = Black700
        )
    }
}


@Composable
private fun PositiveButton(
    @StringRes btnTextRes: Int,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Black700
        )
    ) {
        Text(
            text = stringResource(btnTextRes),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}


@Composable
private fun Title(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Black900
    )
}

@Composable
private fun Description(
    @StringRes descRes: Int,
    fontColor: Color
) {
    Text(
        textAlign = TextAlign.Center,
        text = stringResource(descRes),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em,
        lineHeight = 15.sp,
        color = fontColor
    )
}

@Preview(showBackground = true)
@Composable
fun MyDefaultDialogPreview() {
    MyDefaultDialog(
        titleRes = R.string.will_you_delete,
        descRes = R.string.desc_when_delete_category_change_same_category_notes,
        onClickConfirm = {},
        onClickCancel = {}
    )
}
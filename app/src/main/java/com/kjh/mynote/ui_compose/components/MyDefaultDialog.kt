package com.kjh.mynote.ui_compose.components

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kjh.mynote.R

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 2..
 * Description:
 */

@Composable
fun MyDefaultDialog(
    title: String,
    desc: String? = null,
    descFontColor: Int = R.color.black_500,
    confirmButtonText: String = stringResource(R.string.confirm),
    cancelButtonText: String = stringResource(R.string.cancel),
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
                containerColor = colorResource(R.color.white)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Title(title = title)

                if (!desc.isNullOrBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Description(
                        desc = desc,
                        fontColor = descFontColor
                    )
                }

                Spacer(Modifier.height(24.dp))

                PositiveButton(
                    btnText = confirmButtonText,
                    onClick = onClickConfirm
                )

                Spacer(modifier = Modifier.height(6.dp))

                NegativeButton(
                    btnText = cancelButtonText,
                    onClick = onClickCancel
                )
            }
        }
    }
}

@Composable
private fun NegativeButton(
    btnText: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color = colorResource(R.color.black_700)),
        onClick = onClick
    ) {
        Text(
            text = btnText,
            fontSize = 15.sp,
            color = colorResource(R.color.black_700)
        )
    }
}


@Composable
private fun PositiveButton(
    btnText: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.black_700)
        )
    ) {
        Text(
            text = btnText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.white)
        )
    }
}


@Composable
private fun Title(
    title: String
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = colorResource(R.color.black_900)
    )
}

@Composable
private fun Description(
    desc: String,
    fontColor: Int
) {
    Text(
        text = desc,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em,
        lineHeight = 15.sp,
        color = colorResource(fontColor)
    )
}

@Preview(showBackground = true)
@Composable
fun MyDefaultDialogPreview() {
    MyDefaultDialog(
        title = "정말 삭제하시겠어요?",
        desc = stringResource(R.string.desc_when_delete_category_change_same_category_notes),
        onClickConfirm = {},
        onClickCancel = {}
    )
}
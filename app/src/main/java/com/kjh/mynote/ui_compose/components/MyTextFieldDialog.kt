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
import androidx.compose.ui.graphics.Color
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
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun MyTextFieldDialog(
    title: String,
    desc: String? = null,
    descFontColor: Color = colorResource(R.color.black_500),
    placeHolder: String? = null,
    value: String,
    onValueChanged: (String) -> Unit,
    confirmBtnText: String = stringResource(R.string.confirm),
    cancelBtnText: String = stringResource(R.string.cancel),
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
            modifier = Modifier
                .fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(24.dp))

                MyTextField(
                    value = value,
                    onValueChanged = onValueChanged,
                    placeHolder = placeHolder
                )

                if (!desc.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Description(
                        desc = desc,
                        fontColor = descFontColor
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                PositiveButton(
                    btnText = confirmBtnText,
                    onClick = onClickConfirm
                )

                Spacer(modifier = Modifier.height(6.dp))

                NegativeButton(
                    btnText = cancelBtnText,
                    onClick = onClickCancel
                )
            }
        }
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
    fontColor: Color
) {
    Text(
        text = desc,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em,
        lineHeight = 15.sp,
        color = fontColor
    )
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



@Preview(showBackground = true, heightDp = 400)
@Composable
fun MyTextFieldDialogPreview() {
    MyTextFieldDialog(
        title = "추가할 카테고리를 입력하세요",
        desc = "카테고리명을 수정할 경우 해당 카테고리의 구매노트 카테고리가 전부 변경됩니다.",
        placeHolder = "카테고리를 입력해주세요.",
        value = "",
        onValueChanged = {},
        confirmBtnText = "추가하기",
        cancelBtnText = "취소하기",
        onClickConfirm = {},
        onClickCancel = {}
    )
}
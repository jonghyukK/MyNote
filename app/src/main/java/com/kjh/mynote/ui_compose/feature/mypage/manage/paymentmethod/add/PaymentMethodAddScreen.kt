package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.add

import android.provider.CalendarContract.Colors
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.components.MyBottomButton
import com.kjh.mynote.ui_compose.components.MyTextField
import com.kjh.mynote.ui_compose.components.MyToolBarCompose

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 4..
 * Description:
 */

@Composable
fun PaymentMethodAddRoute(
    viewModel: PaymentMethodAddViewModel = hiltViewModel(),
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PaymentMethodAddSideEffect.NavigateBack -> {
                    onBackClicked()
                }
                is PaymentMethodAddSideEffect.ShowErrorToast -> {
                    Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    PaymentMethodAddScreen(
        uiState = uiState,
        onEvent = viewModel::handleEvent,
        onBackClicked = onBackClicked,
        bottomBtnText = stringResource(R.string.do_register)
    )
}

@Composable
fun PaymentMethodAddScreen(
    uiState: PaymentMethodAddUiState,
    onEvent: (PaymentMethodAddEvent) -> Unit,
    onBackClicked: () -> Unit,
    bottomBtnText: String,
) {
    Scaffold(
        topBar = {
            MyToolBarCompose(
                title = "결제수단 등록",
                onBackButtonClick = onBackClicked
            )
        },
        bottomBar = {
            MyBottomButton(
                modifier = Modifier.imePadding(),
                onClick = { onEvent(PaymentMethodAddEvent.AddPaymentMethod) },
                text = bottomBtnText,
                enabled = uiState.isValidName
            )
        }
    ) { innerPadding ->
        PaymentMethodAddContent(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            paymentMethodName = uiState.paymentMethodName,
            isCheckedDefaultPayment = uiState.isCheckedDefaultPayment,
            onPaymentMethodNameChanged = { value ->
                onEvent(
                    PaymentMethodAddEvent.UpdatePaymentMethodName(
                        value
                    )
                )
            },
            onCheckBoxClicked = { onEvent(PaymentMethodAddEvent.UpdateDefaultPaymentChecked) }
        )
    }
}

@Composable
fun PaymentMethodAddContent(
    modifier: Modifier = Modifier,
    paymentMethodName: String,
    isCheckedDefaultPayment: Boolean,
    onPaymentMethodNameChanged: (String) -> Unit,
    onCheckBoxClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(20.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 12.dp),
            text = stringResource(R.string.payment_method_name),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.black_900)
        )

        MyTextField(
            value = paymentMethodName,
            onValueChanged = onPaymentMethodNameChanged,
            placeHolder = stringResource(R.string.hint_input_payment_method_name)
        )

        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            text = stringResource(R.string.desc_input_payment_method_name),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.01).em,
            color = colorResource(R.color.black_600)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onCheckBoxClicked)
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val icon = if (isCheckedDefaultPayment) {
                    painterResource(R.drawable.ic_selected_checkbox)
                } else {
                    painterResource(R.drawable.ic_unselected_checkbox)
                }

                Icon(
                    painter = icon,
                    contentDescription = "Default Payment CheckBox",
                    tint = colorResource(R.color.colorPrimary)
                )

                Text(
                    text = stringResource(R.string.i_will_register_default_payment_method),
                    fontSize = 14.sp,
                    color = colorResource(R.color.black_500),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.height(50.dp))
        InfoBox()
    }
}

@Composable
fun InfoBox() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, color = colorResource(R.color.colorPrimary)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_lightbulb),
                contentDescription = null,
                tint = colorResource(R.color.colorPrimary)
            )

            Text(
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 18.sp,
                color = colorResource(R.color.black_900),
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.01).em,
                text = stringResource(R.string.desc_recommend_payment_method_name)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_info),
                contentDescription = null,
                tint = colorResource(R.color.colorPrimary)
            )

            Text(
                text = stringResource(R.string.desc_payment_method_name_example1_title),
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 15.sp,
                letterSpacing = (-0.01).em,
                color = colorResource(R.color.black_700)
            )
        }

        Text(
            modifier = Modifier
                .padding(top = 4.dp)
                .align(alignment = Alignment.CenterHorizontally),
            text = stringResource(R.string.desc_payment_method_name_example1),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.01).em,
            color = colorResource(R.color.black_600)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_info),
                contentDescription = null,
                tint = colorResource(R.color.colorPrimary)
            )

            Text(
                text = stringResource(R.string.desc_payment_method_name_example2_title),
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 15.sp,
                letterSpacing = (-0.01).em,
                color = colorResource(R.color.black_700)
            )
        }

        Text(
            modifier = Modifier
                .padding(top = 4.dp)
                .align(alignment = Alignment.CenterHorizontally),
            text = stringResource(R.string.desc_payment_method_name_example2),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.01).em,
            color = colorResource(R.color.black_600)
        )

        Spacer(Modifier.height(28.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PaymehtMethodAddScreenPreview() {
    PaymentMethodAddScreen(
        uiState = PaymentMethodAddUiState(),
        onEvent = {},
        onBackClicked = {},
        bottomBtnText = "등록하기"
    )
}
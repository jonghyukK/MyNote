package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod.addedit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.components.MyBottomButton
import com.kjh.mynote.ui_compose.components.MyCircularProgressIndicator
import com.kjh.mynote.ui_compose.components.MyTextField
import com.kjh.mynote.ui_compose.components.MyToolBarCompose
import com.kjh.mynote.ui_compose.theme.Black500
import com.kjh.mynote.ui_compose.theme.Black600
import com.kjh.mynote.ui_compose.theme.Black700
import com.kjh.mynote.ui_compose.theme.Black900
import com.kjh.mynote.ui_compose.theme.ColorPrimary

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 4..
 * Description:
 */

@Composable
fun PaymentMethodAddEditRoute(
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodAddEditViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentMethodAddEditSideEffect.NavigateUp -> {
                    onNavigateUp()
                }
                is PaymentMethodAddEditSideEffect.ShowErrorToast -> {
                    Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
                }
                is PaymentMethodAddEditSideEffect.ShowKeyBoard -> {
                    focusRequester.requestFocus()
                }
            }
        }
    }

    PaymentMethodAddEditScreen(
        modifier = modifier,
        uiState = uiState,
        onAddEditClick = viewModel::handleEvent,
        onValueChanged = viewModel::handleEvent,
        onToggleDefaultPaymentChecked = viewModel::handleEvent,
        onBackClicked = onNavigateUp,
        focusRequester = focusRequester
    )
}

@Composable
fun PaymentMethodAddEditScreen(
    modifier: Modifier = Modifier,
    uiState: PaymentMethodAddEditUiState,
    onAddEditClick: (PaymentMethodAddEditEvent.AddEditPaymentMethod) -> Unit,
    onValueChanged: (PaymentMethodAddEditEvent.UpdatePaymentMethodName) -> Unit,
    onToggleDefaultPaymentChecked: (PaymentMethodAddEditEvent.ToggleDefaultPaymentChecked) -> Unit,
    onBackClicked: () -> Unit,
    focusRequester: FocusRequester
) {
    Scaffold(
        topBar = {
            MyToolBarCompose(
                titleRes = uiState.viewType.pageTitleRes,
                onBackButtonClick = onBackClicked
            )
        },
        bottomBar = {
            MyBottomButton(
                onClick = { onAddEditClick(PaymentMethodAddEditEvent.AddEditPaymentMethod) },
                btnTextRes = uiState.viewType.bottomButtonTextRes,
                enabled = uiState.isValidName
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            PaymentMethodAddEditContent(
                paymentMethodTextField = {
                    MyTextField(
                        value = uiState.paymentMethodName,
                        onValueChanged = { name -> onValueChanged(PaymentMethodAddEditEvent.UpdatePaymentMethodName(name)) },
                        placeHolderRes = R.string.hint_input_payment_method_name,
                        focusRequester = focusRequester
                    )
                },
                defaultPaymentCheckBox = {
                    DefaultPaymentMethodCheckBox(
                        isCheckedDefaultPayment = uiState.isCheckedDefaultPayment,
                        onCheckBoxClicked = { onToggleDefaultPaymentChecked(PaymentMethodAddEditEvent.ToggleDefaultPaymentChecked) }
                    )
                }
            )

            if (uiState.isLoading) {
                MyCircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodAddEditContent(
    paymentMethodTextField: @Composable () -> Unit,
    defaultPaymentCheckBox: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 12.dp),
            text = stringResource(R.string.payment_method_name),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Black900
        )

        paymentMethodTextField()

        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            text = stringResource(R.string.desc_input_payment_method_name),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.01).em,
            color = Black600
        )

        defaultPaymentCheckBox()

        Spacer(Modifier.height(50.dp))

        InfoBox()
    }
}

@Composable
fun DefaultPaymentMethodCheckBox(
    isCheckedDefaultPayment: Boolean,
    onCheckBoxClicked: () -> Unit,
) {
    val checkBoxIcon = if (isCheckedDefaultPayment) {
        R.drawable.ic_selected_checkbox
    } else {
        R.drawable.ic_unselected_checkbox
    }

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
            Icon(
                painter = painterResource(checkBoxIcon),
                contentDescription = "Default Payment CheckBox",
                tint = ColorPrimary
            )

            Text(
                text = stringResource(R.string.i_will_register_default_payment_method),
                fontSize = 14.sp,
                color = Black500,
                fontWeight = FontWeight.Normal
            )
        }
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
        border = BorderStroke(1.dp, color = ColorPrimary),
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
                tint = ColorPrimary
            )

            Text(
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 18.sp,
                color = Black900,
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
                tint = ColorPrimary
            )

            Text(
                text = stringResource(R.string.desc_payment_method_name_example1_title),
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.01).em,
                color = Black700
            )
        }

        Spacer(Modifier.height(4.dp))

        HighlightedText(
            fullText = stringResource(R.string.desc_payment_method_name_example1),
            targetText = stringResource(R.string.payment_method_name_example1_highlight_text)
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
                tint = ColorPrimary
            )

            Text(
                text = stringResource(R.string.desc_payment_method_name_example2_title),
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.01).em,
                color = Black700
            )
        }

        Spacer(Modifier.height(4.dp))

        HighlightedText(
            fullText = stringResource(R.string.desc_payment_method_name_example2),
            targetText = stringResource(R.string.payment_method_name_example2_highlight_text)
        )

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun HighlightedText(
    fullText: String,
    targetText: String
) {
    val startIndex = fullText.indexOf(targetText)
    val endIndex = startIndex + targetText.length

    val annotatedText = buildAnnotatedString {
        if (startIndex == -1) {
            append(fullText)
        } else {
            append(fullText.substring(0, startIndex))
            pushStyle(
                SpanStyle(
                    color = ColorPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            append(fullText.substring(startIndex, endIndex))
            pop()
        }
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = annotatedText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em,
        color = Black600
    )
}

//@Preview(showBackground = true)
//@Composable
//fun PaymentMethodAddScreenPreview() {
//    PaymentMethodAddEditScreen(
//        uiState = PaymentMethodAddEditUiState(),
//        onEvent = {},
//        onBackClicked = {},
//        focusRequester = remember { FocusRequester() }
//    )
//}
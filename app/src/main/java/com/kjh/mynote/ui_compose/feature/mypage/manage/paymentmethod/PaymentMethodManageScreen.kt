package com.kjh.mynote.ui_compose.feature.mypage.manage.paymentmethod

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.ui_compose.components.MyCircularProgressIndicator
import com.kjh.mynote.ui_compose.components.MyDefaultDialog
import com.kjh.mynote.ui_compose.components.MyToolBarCompose

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */


@Composable
fun PaymentMethodManageRoute(
    modifier: Modifier = Modifier,
    viewModel: PaymentMethodManageViewModel = hiltViewModel(),
    navigateUp: () -> Unit,
    navigateToPaymentMethodAddEdit: (Int) -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentMethodManageSideEffect.ShowErrorToast -> {
                    Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.deleteDialogState.show) {
        MyDefaultDialog(
            title = stringResource(R.string.will_you_delete),
            desc = stringResource(R.string.desc_payment_method_remove),
            descFontColor = R.color.red_500,
            confirmButtonText = stringResource(R.string.yes_i_will_delete),
            cancelButtonText = stringResource(R.string.cancel),
            onClickConfirm = {
                viewModel.handleEvent(PaymentMethodManageEvent.DeletePaymentMethod(uiState.deleteDialogState.paymentMethodId))
            },
            onClickCancel = {
                viewModel.handleEvent(
                    PaymentMethodManageEvent.UpdateDeleteDialogState(DeleteDialogState(false))
                )
            }
        )
    }

    PaymentMethodManageScreen(
        modifier = modifier,
        uiState = uiState,
        onBackClicked = navigateUp,
        onAddClicked = navigateToPaymentMethodAddEdit,
        onEditClicked = navigateToPaymentMethodAddEdit,
        onDeleteClicked = { paymentMethodId ->
            viewModel.handleEvent(PaymentMethodManageEvent.UpdateDeleteDialogState(DeleteDialogState(true, paymentMethodId)))
        }
    )
}

@Composable
fun PaymentMethodManageScreen(
    modifier: Modifier = Modifier,
    uiState: PaymentMethodManageUiState,
    onBackClicked: () -> Unit,
    onAddClicked: (paymentMethodId: Int) -> Unit,
    onEditClicked: (paymentMethodId: Int) -> Unit,
    onDeleteClicked: (paymentMethodId: Int) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MyToolBarCompose(
                title = stringResource(R.string.manage_payment_method),
                onBackButtonClick = onBackClicked,
                rightFirstImageRes = R.drawable.ic_add_24,
                rightFirstImageDesc = "Add PaymentMethod",
                rightFirstImageClick = { onAddClicked(-1) }
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding)) {
            if (uiState.isLoading) {
                MyCircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            PaymentMethodList(
                modifier = modifier,
                paymentMethodItems = uiState.paymentMethods,
                onClickEdit = onEditClicked,
                onClickDelete = onDeleteClicked
            )
        }
    }
}

@Composable
fun PaymentMethodList(
    modifier: Modifier = Modifier,
    paymentMethodItems: List<PaymentMethodUiModel>,
    onClickEdit: (Int) -> Unit,
    onClickDelete: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        items(paymentMethodItems, key = { it.paymentMethodId }) { item ->
            PaymentMethodItem(
                item = item,
                onEditClicked = { onClickEdit(item.paymentMethodId) },
                onDeleteClicked = { onClickDelete(item.paymentMethodId) }
            )
        }
    }
}

@Composable
fun PaymentMethodItem(
    modifier: Modifier = Modifier,
    item: PaymentMethodUiModel,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = 20.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(end = 6.dp),
            text = item.paymentMethodName,
            fontSize = 16.sp,
            color = colorResource(R.color.black_900)
        )

        if (item.isDefault) {
            Text(
                text = "기본 결제수단",
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.colorPrimary),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.weight(1f))
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = onEditClicked
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit PaymentMethod"
            )
        }

        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = onDeleteClicked
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete PaymentMethod"
            )
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = colorResource(R.color.black_50)
    )
}

@Preview(showBackground = true)
@Composable
fun PaymentMethodItemPreview() {
    PaymentMethodItem(
        item = PaymentMethodUiModel(paymentMethodName = "hello"),
        onEditClicked = {},
        onDeleteClicked = {}
    )
}
package com.kjh.mynote.ui_compose.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjh.mynote.R
import com.kjh.mynote.ui_compose.components.MyToolBarCompose
import com.kjh.mynote.ui_compose.theme.Black50
import com.kjh.mynote.ui_compose.theme.Black600
import com.kjh.mynote.ui_compose.theme.Black900

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    navigateToCategoryManage: () -> Unit = {},
    navigateToPaymentMethodManage: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MyToolBarCompose(
                title = stringResource(R.string.my_page),
                showBackButton = false
            )
        }
    ) { innerPadding ->
        MyPageContent(
            modifier = modifier.padding(innerPadding),
            onCategoryManageClick = navigateToCategoryManage,
            onPaymentMethodManageClick = navigateToPaymentMethodManage
        )
    }
}

@Composable
fun MyPageContent(
    modifier: Modifier = Modifier,
    onCategoryManageClick: () -> Unit = {},
    onPaymentMethodManageClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .background(color = Black50)
    ) {
        SectionTitle(title = stringResource(R.string.title_manage_category))
        SectionContents(
            title = stringResource(R.string.title_manage_category),
            onClick = onCategoryManageClick
        )

        SectionTitle(title = stringResource(R.string.manage_payment_method))
        SectionContents(
            title = stringResource(R.string.manage_payment_method),
            onClick = onPaymentMethodManageClick
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 17.sp,
        color = Black600,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
    )
}

@Composable
fun SectionContents(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(50.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = title,
            fontSize = 16.sp,
            color = Black900
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .size(44.dp)
                .padding(8.dp),
            tint = Black600,
            painter = painterResource(R.drawable.ic_chevron_right_24_purple),
            contentDescription = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MyPageScreen()
}
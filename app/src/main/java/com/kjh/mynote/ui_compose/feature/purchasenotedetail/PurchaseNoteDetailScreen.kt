package com.kjh.mynote.ui_compose.feature.purchasenotedetail

import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kjh.mynote.R
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui_compose.components.MyCircularProgressIndicator
import com.kjh.mynote.ui_compose.components.MyDefaultDialog
import com.kjh.mynote.ui_compose.components.MyToolBarCompose
import com.kjh.mynote.ui_compose.theme.Black50
import com.kjh.mynote.ui_compose.theme.Black600
import com.kjh.mynote.ui_compose.theme.Black800
import com.kjh.mynote.ui_compose.theme.ColorPrimary
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithFormat
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 13..
 * Description:
 */

@Composable
fun PurchaseNoteDetailRoute(
    viewModel: PurchaseNoteDetailViewModel = hiltViewModel(),
    navigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effects ->
            when (effects) {
                is PurchaseNoteDetailSideEffect.NavigateUp -> {
                    navigateUp()
                }
                is PurchaseNoteDetailSideEffect.ShowErrorToast -> {
                    Toast.makeText(context, effects.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    PurchaseNoteDetailScreen(
        isLoading = state.isLoading,
        purchaseName = state.purchaseNoteItem.purchaseName,
        categoryName = state.purchaseNoteItem.category?.categoryName ?: "카테고리 없음",
        paymentMethodName = state.purchaseNoteItem.paymentMethod?.paymentMethodName ?: "결제수단 없음",
        paidDate = state.purchaseNoteItem.purchaseDate.toStringWithFormat(AppConstants.DATE_FORMAT_YYYY_M_D_E),
        paidPrice = state.purchaseNoteItem.purchasePrice.toComma() + "원",
        purchasePlace = state.purchaseNoteItem.placeInfo,
        placeImages = state.purchaseNoteItem.images,
        onClickBackButton = navigateUp,
        onClickDelete = {
            viewModel.handleEvent(PurchaseNoteDetailUiEvent.UpdateDeleteDialogVisibility(true))
        }
    )

    if (state.isVisibleDeleteDialog) {
        MyDefaultDialog(
            titleRes = R.string.will_you_delete_this_purchase_note,
            confirmButtonTextRes = R.string.yes_i_will_delete,
            cancelButtonTextRes = R.string.cancel,
            onClickConfirm = {
                viewModel.handleEvent(PurchaseNoteDetailUiEvent.DeletePurchaseNote)
            },
            onClickCancel = {
                viewModel.handleEvent(PurchaseNoteDetailUiEvent.UpdateDeleteDialogVisibility(false))
            }
        )
    }
}

@Composable
fun PurchaseNoteDetailScreen(
    isLoading: Boolean,
    purchaseName: String,
    categoryName: String,
    paymentMethodName: String,
    paidDate: String,
    paidPrice: String,
    purchasePlace: PlaceInfoUiModel?,
    placeImages: List<String>?,
    onClickBackButton: () -> Unit,
    onClickDelete: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MyToolBarCompose(
                showBackButton = true,
                onBackButtonClick = onClickBackButton,
                rightFirstImageRes = R.drawable.ic_delete_24,
                rightFirstImageDesc = "Delete PurchaseNote",
                rightFirstImageClick = onClickDelete
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            PurchaseNoteDetailContent(
                purchaseName = purchaseName,
                categoryName = categoryName,
                paymentMethodName = paymentMethodName,
                paidDate = paidDate,
                paidPrice = paidPrice,
                purchasePlace = purchasePlace,
                placeImages = placeImages
            )

            if (isLoading) {
                MyCircularProgressIndicator()
            }
        }
    }
}

@Composable
fun PurchaseNoteDetailContent(
    purchaseName: String,
    categoryName: String,
    paymentMethodName: String,
    paidDate: String,
    paidPrice: String,
    purchasePlace: PlaceInfoUiModel?,
    placeImages: List<String>? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Black50)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = Color.White)
                .padding(20.dp),
        ) {
            Text(
                text = purchaseName,
                fontSize = 24.sp,
                color = Black800,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(18.dp))

            TitleWithTextRow(
                titleRes = R.string.category,
                value = categoryName
            )

            TitleWithTextRow(
                titleRes = R.string.payment_method,
                value = paymentMethodName
            )

            TitleWithTextRow(
                titleRes = R.string.purchase_date,
                value = paidDate
            )

            TitleWithTextRow(
                titleRes = R.string.purchase_price,
                value = paidPrice
            )

            if (purchasePlace != null) {
                TitleWithSelectableTextRow(
                    titleRes = R.string.purchase_place,
                    value = purchasePlace.placeName,
                    onClickValue = {}
                )
            }

            if (!placeImages.isNullOrEmpty()) {
                Spacer(Modifier.height(20.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(placeImages, key = { it }) { imageUrl ->
                        PlaceImageItem(imageUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceImageItem(
    imageUrl: String
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .size(100.dp)
            .clip(shape = RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun TitleWithTextRow(
    @StringRes titleRes: Int,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(titleRes),
            fontSize = 16.sp,
            color = Black600,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 16.sp,
            color = Black800,
            letterSpacing = (-0.01).em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(3f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TitleWithSelectableTextRow(
    @StringRes titleRes: Int,
    value: String,
    onClickValue: () -> Unit,
    isVisibleArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(titleRes),
            fontSize = 16.sp,
            color = Black600,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(3f)
                .clickable(onClick = onClickValue),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                color = Black800,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )

            if (isVisibleArrow) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ColorPrimary
                )
            }
        }
    }
}


//
//@Preview(showBackground = true)
//@Composable
//fun PurchaseNoteDetailContentPreview() {
//    PurchaseNoteDetailContent(
//        purchaseName = "편의점",
//        categoryName = "편의점/담배",
//        paymentMethodName = "현대 카드",
//        paidDate = "2025년 1월 14일 (화)",
//        paidPrice = "5000원",
//        purchasePlace = PlaceInfoUiModel.default
//    )
//}
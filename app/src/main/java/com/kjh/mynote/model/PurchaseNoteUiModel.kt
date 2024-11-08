package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchasePlaceInfo
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@Parcelize
data class PurchaseNoteUiModel(
    val id: Int,
    val purchaseDate: Long,
    val purchasePrice: Long,
    val category: String,
    val images: List<String>? = null,
    val purchasePlaceInfo: PurchasePlaceInfoUiModel? = null
): Parcelable

@Parcelize
data class PurchasePlaceInfoUiModel(
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String? = null,
    val x: String,
    val y: String
): Parcelable

fun PurchaseNote.toUiModel() = PurchaseNoteUiModel(
    id = id,
    purchaseDate = purchaseDate,
    purchasePrice = purchasePrice,
    category = category,
    images = images,
    purchasePlaceInfo = purchasePlaceInfo?.toUiModel()
)

fun PurchasePlaceInfo.toUiModel() = PurchasePlaceInfoUiModel(
    placeName = placeName,
    placeAddress = placeAddress,
    placeRoadAddress = placeRoadAddress,
    x = x,
    y = y
)
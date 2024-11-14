package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.Category
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchasePlaceInfo
import com.kjh.mynote.utils.extensions.toLocalDate
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@Parcelize
data class PurchaseNoteUiModel(
    val id: Int,
    val purchaseDate: Long,
    val purchaseLocalDate: LocalDate,
    val purchasePrice: Long,
    val purchaseName: String,
    val category: CategoryUiModel? = null,
    val images: List<String>? = null,
    val purchasePlaceInfo: PurchasePlaceInfoUiModel? = null,
): Parcelable

@Parcelize
data class PurchasePlaceInfoUiModel(
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String,
    val x: String,
    val y: String
): Parcelable

@Parcelize
data class CategoryUiModel(
    val id: Int,
    val categoryName: String
): Parcelable

fun PurchaseNote.toUiModel() = PurchaseNoteUiModel(
    id = id,
    purchaseDate = purchaseDate,
    purchaseLocalDate = purchaseDate.toLocalDate(),
    purchasePrice = purchasePrice,
    purchaseName = purchaseName,
    category = category?.toUiModel(),
    images = images,
    purchasePlaceInfo = purchasePlaceInfo?.toUiModel()
)

fun List<PurchaseNote>.toUiModel() = map(PurchaseNote::toUiModel)

fun PurchasePlaceInfo.toUiModel() = PurchasePlaceInfoUiModel(
    placeName = placeName,
    placeAddress = placeAddress,
    placeRoadAddress = placeRoadAddress,
    x = x,
    y = y
)

fun Category.toUiModel() = CategoryUiModel(
    id = id,
    categoryName = categoryName
)

fun CategoryUiModel.toDomainModel() = Category(
    id = id,
    categoryName = categoryName
)
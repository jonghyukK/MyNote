package com.kjh.mynote.model

import android.os.Parcelable
import com.example.domain.model.PurchaseNote
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
    val paymentMethod: PaymentMethodUiModel? = null,
    val images: List<String>? = null,
    val placeInfo: PlaceInfoUiModel? = null,
): Parcelable

/**
 *  PurchaseNote (domain) -> PurchaseNoteUiModel (presentation)
 */
fun PurchaseNote.toUiModel() = PurchaseNoteUiModel(
    id = id,
    purchaseDate = purchaseDate,
    purchaseLocalDate = purchaseDate.toLocalDate(),
    purchasePrice = purchasePrice,
    purchaseName = purchaseName,
    category = category?.toUiModel(),
    paymentMethod = paymentMethod?.toUiModel(),
    images = images,
    placeInfo = placeInfo?.toUiModel()
)

fun List<PurchaseNote>.toUiModel() = map(PurchaseNote::toUiModel)

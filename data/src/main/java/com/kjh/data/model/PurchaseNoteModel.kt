package com.kjh.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.domain.model.PlaceInfo
import com.example.domain.model.PurchaseNote
import com.kjh.data.model.entity.CategoryEntity
import com.kjh.data.model.entity.PaymentMethodEntity
import com.kjh.data.model.entity.PurchaseNoteEntity
import com.kjh.data.model.entity.toDomainModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 31..
 * Description:
 */

data class PurchaseNoteModel(
    @Embedded
    val purchaseNote: PurchaseNoteEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "paymentMethodId",
        entityColumn = "paymentMethodId"
    )
    val paymentMethod: PaymentMethodEntity?
)

/**
 *  PurchaseNoteModel (data) -> PurchaseNote (domain)
 */
fun PurchaseNoteModel.toDomainModel() =
    PurchaseNote(
        id = purchaseNote.id,
        purchaseDate = purchaseNote.purchaseDate,
        purchasePrice = purchaseNote.purchasePrice,
        purchaseName = purchaseNote.purchaseName,
        category = category?.toDomainModel(),
        paymentMethod = paymentMethod?.toDomainModel(),
        images = purchaseNote.images,
        placeInfo =  if (purchaseNote.placeId == null) {
            null
        } else {
            PlaceInfo(
                id = purchaseNote.placeId,
                name = purchaseNote.placeName ?: "",
                address = purchaseNote.placeAddress ?: "",
                roadAddress = purchaseNote.placeRoadAddress ?: "",
                x = purchaseNote.x ?: "",
                y = purchaseNote.y ?: ""
            )
        }
    )

fun List<PurchaseNoteModel>.toDomainModel() = map(PurchaseNoteModel::toDomainModel)
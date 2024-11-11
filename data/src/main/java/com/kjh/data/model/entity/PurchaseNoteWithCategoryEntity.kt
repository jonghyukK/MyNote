package com.kjh.data.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.domain.model.PurchaseNote

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

data class PurchaseNoteWithCategoryEntity(

    @Embedded val purchaseNote: PurchaseNoteEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

fun PurchaseNoteWithCategoryEntity.toDomainModel() =
    PurchaseNote(
        id = purchaseNote.id,
        purchaseDate = purchaseNote.purchaseDate,
        purchasePrice = purchaseNote.purchasePrice,
        purchaseName = purchaseNote.purchaseName,
        category = category?.toDomainModel(),
        images = purchaseNote.images,
        purchasePlaceInfo = purchaseNote.purchasePlaceInfo
    )

package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.domain.model.PurchaseNote

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@Entity(
    tableName = "purchase",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class PurchaseNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val purchaseDate: Long,
    val purchasePrice: Long,
    val purchaseName: String,
    val images: List<String>? = null,
    val categoryId: Int?,
    val placeId: String?,
    val placeName: String?,
    val placeAddress: String?,
    val placeRoadAddress: String?,
    val x: String?,
    val y: String?
)

/**
 *  PurchaseNote (domain) -> PurchaseNoteEntity (data)
 */
fun PurchaseNote.toEntity(): PurchaseNoteEntity {
    return PurchaseNoteEntity(
        id = id,
        purchaseDate = purchaseDate,
        purchasePrice = purchasePrice,
        purchaseName = purchaseName,
        categoryId = category?.id,
        images = images,
        placeId = placeInfo?.id,
        placeName = placeInfo?.name,
        placeAddress = placeInfo?.address,
        placeRoadAddress = placeInfo?.roadAddress,
        x = placeInfo?.x,
        y = placeInfo?.y
    )
}
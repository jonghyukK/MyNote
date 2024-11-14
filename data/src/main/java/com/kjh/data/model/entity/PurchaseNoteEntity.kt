package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.domain.model.PlaceInfo
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchasePlaceInfo
import com.kjh.data.db.DBTypeConverters

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
    @TypeConverters(DBTypeConverters::class)
    val placeInfo: PlaceInfo? = null,
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
        placeInfo = placeInfo
    )
}
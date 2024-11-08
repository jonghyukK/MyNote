package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.domain.model.PurchaseNote
import com.example.domain.model.PurchasePlaceInfo
import com.kjh.data.db.DBTypeConverters

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@Entity(tableName = "purchase")
data class PurchaseNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val purchaseDate: Long,
    val purchasePrice: Long,
    val category: String,
    val images: List<String>? = null,

    @TypeConverters(DBTypeConverters::class)
    val purchasePlaceInfo: PurchasePlaceInfo? = null,
)

fun PurchaseNoteEntity.toDomainModel() = PurchaseNote(
    id = id,
    purchaseDate = purchaseDate,
    purchasePrice = purchasePrice,
    category = category,
    images = images,
    purchasePlaceInfo = purchasePlaceInfo
)

fun List<PurchaseNoteEntity>.toDomainModel() = map(PurchaseNoteEntity::toDomainModel)

fun PurchaseNote.toEntity() = PurchaseNoteEntity(
    purchaseDate = purchaseDate,
    purchasePrice = purchasePrice,
    category = category,
    images = images,
    purchasePlaceInfo = purchasePlaceInfo
)


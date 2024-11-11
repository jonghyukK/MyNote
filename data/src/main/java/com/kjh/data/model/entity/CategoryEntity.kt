package com.kjh.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Category

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryName: String
)

fun CategoryEntity.toDomainModel() = Category(
    id = id,
    categoryName = categoryName
)

fun List<CategoryEntity>.toDomainModel() = map(CategoryEntity::toDomainModel)

fun Category.toEntity() = CategoryEntity(
    id = id,
    categoryName = categoryName
)
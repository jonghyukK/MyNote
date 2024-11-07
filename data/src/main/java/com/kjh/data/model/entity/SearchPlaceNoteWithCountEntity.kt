package com.kjh.data.model.entity

import com.example.domain.model.SearchPlaceNoteWithCount

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

data class SearchPlaceNoteWithCountEntity(
    val id: Int,
    val placeName: String,
    val placeAddress: String,
    val placeRoadAddress: String? = null,
    val count: Int
)

fun SearchPlaceNoteWithCountEntity.toDomainModel() =
    SearchPlaceNoteWithCount(
        id = id,
        placeName = placeName,
        placeAddress = placeAddress,
        placeRoadAddress = placeRoadAddress,
        count = count
    )

fun List<SearchPlaceNoteWithCountEntity>.toDomainModel() = map(SearchPlaceNoteWithCountEntity::toDomainModel)
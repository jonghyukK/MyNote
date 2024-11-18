package com.example.domain.model

import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */
data class FilteredSearchPlaceNotes(
    val date: LocalDate,
    val placeNotes: List<PlaceNote>
)
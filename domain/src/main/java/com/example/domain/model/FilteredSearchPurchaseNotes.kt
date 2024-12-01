package com.example.domain.model

import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */
data class FilteredSearchPurchaseNotes(
    val date: LocalDate?,
    val purchaseNotes: List<PurchaseNote>
)
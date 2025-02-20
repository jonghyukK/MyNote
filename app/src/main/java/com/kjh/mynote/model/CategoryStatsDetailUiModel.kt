package com.kjh.mynote.model

import com.example.domain.model.CategoryStats
import com.example.domain.model.PurchaseNameStats

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 20..
 * Description:
 */
data class CategoryStatsDetailUiModel(
    val categoryStats: CategoryStatsUiModel,
    val purchaseNameStatsList: List<PurchaseNameStatsUiModel>
)
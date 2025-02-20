package com.kjh.mynote.ui.features.statistics.category.adapter.empty

import com.kjh.mynote.databinding.VhCategoryStatisticsEmptyBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.statistics.category.CategoryStatisticsUiItem

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 21..
 * Description:
 */
class CategoryStatisticsEmptyItemViewHolder(
    private val binding: VhCategoryStatisticsEmptyBinding
): BaseViewHolder<CategoryStatisticsUiItem.Empty>(binding.root) {

    override fun bind(item: CategoryStatisticsUiItem.Empty) {
        super.bind(item)
    }
}
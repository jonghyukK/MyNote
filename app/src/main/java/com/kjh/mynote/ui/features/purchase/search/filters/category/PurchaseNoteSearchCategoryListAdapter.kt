package com.kjh.mynote.ui.features.purchase.search.filters.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhPurchaseNoteSearchCategoryListItemBinding
import com.kjh.mynote.ui.features.category.list.CategoryListItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 22..
 * Description:
 */
class PurchaseNoteSearchCategoryListAdapter(
    private val categoryFilterClickAction: (Int) -> Unit
): ListAdapter<CategoryListItem, PurchaseNoteSearchCategoryListItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PurchaseNoteSearchCategoryListItemViewHolder(
        VhPurchaseNoteSearchCategoryListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), categoryFilterClickAction
    )

    override fun onBindViewHolder(
        holder: PurchaseNoteSearchCategoryListItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<CategoryListItem>() {
            override fun areItemsTheSame(
                oldItem: CategoryListItem,
                newItem: CategoryListItem
            ): Boolean = oldItem.categoryItem.id == newItem.categoryItem.id

            override fun areContentsTheSame(
                oldItem: CategoryListItem,
                newItem: CategoryListItem
            ): Boolean = oldItem == newItem
        }
    }
}

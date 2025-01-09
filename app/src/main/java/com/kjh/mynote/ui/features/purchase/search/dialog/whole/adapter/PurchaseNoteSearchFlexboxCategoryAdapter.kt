package com.kjh.mynote.ui.features.purchase.search.dialog.whole.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.model.Filters

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 27..
 * Description:
 */
class PurchaseNoteSearchFlexboxCategoryAdapter(
    private val categoryItemClickAction: (Filters.Category) -> Unit
): ListAdapter<Filters.Category, PurchaseNoteSearchFlexboxCategoryItemViewHolder>(
    UI_MODEL_COMPARATOR
) {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PurchaseNoteSearchFlexboxCategoryItemViewHolder(
        VhFilterCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), categoryItemClickAction
    )

    override fun onBindViewHolder(
        holder: PurchaseNoteSearchFlexboxCategoryItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<Filters.Category>() {
                override fun areItemsTheSame(
                    oldItem: Filters.Category,
                    newItem: Filters.Category
                ): Boolean = oldItem.categoryItem.id == newItem.categoryItem.id

                override fun areContentsTheSame(
                    oldItem: Filters.Category,
                    newItem: Filters.Category
                ): Boolean = oldItem == newItem
            }
    }
}

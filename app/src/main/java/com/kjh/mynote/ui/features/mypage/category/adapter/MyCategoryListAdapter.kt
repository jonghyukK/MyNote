package com.kjh.mynote.ui.features.mypage.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhMyCategoryListItemBinding
import com.kjh.mynote.model.CategoryUiModel

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */
class MyCategoryListAdapter(
    private val editClickAction: (CategoryUiModel) -> Unit,
    private val deleteClickAction: (CategoryUiModel) -> Unit
): ListAdapter<CategoryUiModel, MyCategoryListItemViewHolder>(UI_MODEL_COMPARATOR) {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = MyCategoryListItemViewHolder(
        VhMyCategoryListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), editClickAction, deleteClickAction
    )

    override fun onBindViewHolder(holder: MyCategoryListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<CategoryUiModel>() {
            override fun areItemsTheSame(
                oldItem: CategoryUiModel,
                newItem: CategoryUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CategoryUiModel,
                newItem: CategoryUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
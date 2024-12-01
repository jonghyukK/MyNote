package com.kjh.mynote.ui.common.dialog.sort.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.domain.model.SortType
import com.kjh.mynote.databinding.VhSortListItemBinding
import com.kjh.mynote.ui.common.dialog.sort.SortItem

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 30..
 * Description:
 */
class SortListAdapter(
    private val sortItemClickAction: (SortType) -> Unit
): ListAdapter<SortItem, SortItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SortItemViewHolder(
            VhSortListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), sortItemClickAction
        )

    override fun onBindViewHolder(holder: SortItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR =
            object : DiffUtil.ItemCallback<SortItem>() {
                override fun areItemsTheSame(
                    oldItem: SortItem,
                    newItem: SortItem
                ): Boolean = oldItem.type == newItem.type

                override fun areContentsTheSame(
                    oldItem: SortItem,
                    newItem: SortItem
                ): Boolean = oldItem == newItem
            }
    }
}

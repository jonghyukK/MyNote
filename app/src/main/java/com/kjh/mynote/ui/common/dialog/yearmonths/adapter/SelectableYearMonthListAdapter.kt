package com.kjh.mynote.ui.common.dialog.yearmonths.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhSelectableYearMonthListItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.dialog.yearmonths.SelectableYearMonthItem
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.toStringWithPattern
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description:
 */
class SelectableYearMonthListAdapter(
    private val onItemClickAction: (LocalDate) -> Unit
): ListAdapter<SelectableYearMonthItem, SelectableYearMonthItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SelectableYearMonthItemViewHolder(
        VhSelectableYearMonthListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ), onItemClickAction
    )

    override fun onBindViewHolder(holder: SelectableYearMonthItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<SelectableYearMonthItem>() {
            override fun areItemsTheSame(
                oldItem: SelectableYearMonthItem,
                newItem: SelectableYearMonthItem
            ): Boolean = oldItem.date == newItem.date

            override fun areContentsTheSame(
                oldItem: SelectableYearMonthItem,
                newItem: SelectableYearMonthItem
            ): Boolean = oldItem == newItem
        }
    }
}
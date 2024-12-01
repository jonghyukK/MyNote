package com.kjh.mynote.ui.common.dialog.sort.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhSortListItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.common.dialog.sort.SortItem
import com.kjh.mynote.ui.common.dialog.sort.SortType
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 1..
 * Description:
 */

class SortItemViewHolder(
    private val binding: VhSortListItemBinding,
    private val sortItemClickAction: (SortType) -> Unit
): BaseViewHolder<SortItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> sortItemClickAction(item.type)}
        }
    }

    override fun bind(item: SortItem) {
        super.bind(item)

        with (binding) {
            ivCheck.isVisible = item.isSelected
            tvSortName.text = item.type.title

            val textColor = if (item.isSelected) selectedColor else normalColor
            tvSortName.setTextColorRes(textColor)
        }
    }

    companion object {
        private val selectedColor = R.color.colorPrimary
        private val normalColor = R.color.black_800
    }
}
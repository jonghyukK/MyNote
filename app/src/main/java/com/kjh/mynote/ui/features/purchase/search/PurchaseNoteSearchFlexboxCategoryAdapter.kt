package com.kjh.mynote.ui.features.purchase.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhFilterCategoryItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.utils.extensions.getDrawableCompat
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.setTextColorRes

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 27..
 * Description:
 */
class PurchaseNoteSearchFlexboxCategoryAdapter(
    private val categoryItemClickAction: (Filters.Category) -> Unit
): ListAdapter<Filters.Category, PurchaseNoteSearchFlexboxCategoryItemViewHolder>(UI_MODEL_COMPARATOR) {
    
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

class PurchaseNoteSearchFlexboxCategoryItemViewHolder(
    private val binding: VhFilterCategoryItemBinding,
    private val categoryItemClickAction: (Filters.Category) -> Unit
): BaseViewHolder<Filters.Category>(binding.root) {
    
    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> categoryItemClickAction(item) }
        }
    }

    override fun bind(item: Filters.Category) {
        super.bind(item)

        with (binding) {
            tvCategoryName.text = item.categoryItem.categoryName

            if (item.isApplied()) {
                root.background = context.getDrawableCompat(R.drawable.shape_s_color_primary_c_20)
                tvCategoryName.setTextColorRes(R.color.white)
            } else {
                root.background = context.getDrawableCompat(R.drawable.shape_s_white_c_20_l_color_primary)
                tvCategoryName.setTextColorRes(R.color.colorPrimary)
            }
        }
    }
}
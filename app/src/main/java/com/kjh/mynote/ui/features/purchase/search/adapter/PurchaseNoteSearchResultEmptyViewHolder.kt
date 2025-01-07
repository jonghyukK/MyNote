package com.kjh.mynote.ui.features.purchase.search.adapter

import androidx.core.content.ContextCompat
import com.kjh.mynote.R
import com.kjh.mynote.databinding.LayoutEmptySearchResultsBinding
import com.kjh.mynote.ui.base.BaseViewHolder

/**
 * Created by kangjonghyuk.
 * Created On 2025. 1. 7..
 * Description:
 */
class PurchaseNoteSearchResultEmptyViewHolder(
    private val binding: LayoutEmptySearchResultsBinding,
) : BaseViewHolder<Unit>(binding.root) {

    override fun bind(item: Unit) {
        super.bind(item)

        binding.root.apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            val lp = layoutParams
            lp.height =
                context.resources.getDimensionPixelSize(R.dimen.purchase_note_empty_view_height)
            layoutParams = lp
        }
    }
}


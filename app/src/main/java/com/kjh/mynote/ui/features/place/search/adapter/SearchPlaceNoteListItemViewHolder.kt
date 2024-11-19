package com.kjh.mynote.ui.features.place.search.adapter

import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhSearchPlaceNoteListItemBinding
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.search.SearchResultItem
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.ifNullOrEmpty
import com.kjh.mynote.utils.extensions.onThrottleClick

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */
class SearchPlaceNoteListItemViewHolder(
    private val binding: VhSearchPlaceNoteListItemBinding,
    private val itemClickAction: (SearchResultItem) -> Unit
): BaseViewHolder<SearchResultItem>(binding.root) {

    init {
        itemView.onThrottleClick {
            bindItem?.let { item -> itemClickAction.invoke(item) }
        }
    }

    override fun bind(item: SearchResultItem) {
        super.bind(item)

        with (binding) {
            tvResultPlaceName.highlightText(item.item.placeName, item.queryText)

            val address = item.item.placeRoadAddress.ifNullOrEmpty(item.item.placeAddress)
            tvResultAddress.highlightText(address, item.queryText)

            tvPlaceCount.isVisible = item.count > 1
            tvPlaceCount.text = context.getString(R.string.format_plus_value, item.count)
        }
    }
}
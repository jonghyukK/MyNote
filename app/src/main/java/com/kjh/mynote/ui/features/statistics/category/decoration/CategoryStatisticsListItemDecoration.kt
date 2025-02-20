package com.kjh.mynote.ui.features.statistics.category.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.ui.features.statistics.category.CategoryStatisticsUiItem
import com.kjh.mynote.utils.extensions.dpToPx

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 26..
 * Description:
 */
class CategoryStatisticsListItemDecoration: RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val adapter = parent.adapter as? ListAdapter<CategoryStatisticsUiItem, *>
        val currentItem = adapter?.currentList?.get(position) ?: return
        val prevItem = adapter.currentList.getOrNull(position - 1)

        when {
            currentItem is CategoryStatisticsUiItem.PurchaseNoteContents &&
                    prevItem is CategoryStatisticsUiItem.PurchaseNoteDate -> {
                outRect.top = 12.dpToPx()
            }
            currentItem is CategoryStatisticsUiItem.PurchaseNoteContents -> {
                outRect.top = 8.dpToPx()
            }
            currentItem is CategoryStatisticsUiItem.PurchaseNoteDate -> {
                outRect.top = 20.dpToPx()
            }
            else -> {
                outRect.set(0, 0, 0, 0)
            }
        }
    }
}
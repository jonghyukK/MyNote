package com.kjh.mynote.ui.features.purchase.statistics.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.ui.features.purchase.statistics.PurchaseNoteStaticsUiItem
import com.kjh.mynote.utils.extensions.dpToPx

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 23..
 * Description:
 */
class PurchaseNoteStatisticsItemDecoration: RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val listAdapter = parent.adapter as? ListAdapter<PurchaseNoteStaticsUiItem, *>
        listAdapter?.let { adapter ->
            val currentItem = adapter.currentList[position] ?: return
            val prevItem = adapter.currentList.getOrNull(position - 1)

            when {
                currentItem is PurchaseNoteStaticsUiItem.CategoryStatsItem &&
                        prevItem is PurchaseNoteStaticsUiItem.PieChartItem -> {
                    outRect.top = 12.dpToPx()
                }
                currentItem is PurchaseNoteStaticsUiItem.CategoryStatsItem -> {
                    outRect.top = 6.dpToPx()
                }

                else -> {
                    outRect.set(0, 0, 0, 0)
                }
            }
        }

    }
}
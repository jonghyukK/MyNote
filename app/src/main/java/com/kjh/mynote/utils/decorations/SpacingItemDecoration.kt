package com.kjh.mynote.utils.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.DiffUtil.DiffResult.NO_POSITION
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.utils.extensions.dpToPx

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 18..
 * Description:
 */
class SpacingItemDecoration(
    private val left: Int = 0,
    private val top: Int = 0,
    private val right: Int = 0,
    private val bottom: Int = 0,
    private val exceptFirstItem: Boolean = true
): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.layoutManager?.getPosition(view) ?: NO_POSITION

        if (exceptFirstItem && position == 0) {
            return
        }

        if (left > 0) {
            outRect.left = left.dpToPx()
        }

        if (right > 0) {
            outRect.right = right.dpToPx()
        }

        if (top > 0) {
            outRect.top = top.dpToPx()
        }

        if (bottom > 0) {
            outRect.bottom = bottom.dpToPx()
        }
    }
}
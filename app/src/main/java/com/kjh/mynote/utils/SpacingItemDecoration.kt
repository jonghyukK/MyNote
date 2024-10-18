package com.kjh.mynote.utils

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
    private val spacing: Int
): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.layoutManager?.getPosition(view) ?: NO_POSITION
        if (position != 0) {
            outRect.top = spacing.dpToPx()
        }
    }
}
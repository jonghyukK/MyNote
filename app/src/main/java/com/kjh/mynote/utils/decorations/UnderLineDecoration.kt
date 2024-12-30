package com.kjh.mynote.utils.decorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kjh.mynote.R
import com.kjh.mynote.utils.extensions.dpToPx

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */
class UnderLineDecoration(
    private val context: Context,
    @ColorRes private val colorRes: Int = R.color.black_200,
    private val height: Int = 1
): RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, colorRes)
        style = Paint.Style.FILL
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + height.dpToPx()

            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}
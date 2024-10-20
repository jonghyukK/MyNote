package com.kjh.mynote.ui.common.listener

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 20..
 * Description:
 */

class OnNestedHorizontalTouchListener: RecyclerView.SimpleOnItemTouchListener() {
    private var initialX = 0f
    private var initialY = 0f

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            rv.parent.requestDisallowInterceptTouchEvent(true)
            initialX = e.rawX
            initialY = e.rawY

        } else if (e.action == MotionEvent.ACTION_MOVE) {
            val xDiff = abs(e.rawX - initialX)
            val yDiff = abs(e.rawY - initialY)
            if (yDiff > xDiff) {
                rv.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onInterceptTouchEvent(rv, e)
    }
}
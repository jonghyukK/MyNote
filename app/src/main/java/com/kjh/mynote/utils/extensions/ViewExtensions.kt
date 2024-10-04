package com.kjh.mynote.utils.extensions

import android.view.View
import com.kjh.mynote.ui.common.listener.OnThrottleClickListener

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 24..
 * Description: View 관련 확장 함수들.
 */

fun View.onThrottleClick(action: (v: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener))
}

fun View.setOnThrottleClickListener(listener: View.OnClickListener) {
    setOnClickListener(OnThrottleClickListener(listener))
}
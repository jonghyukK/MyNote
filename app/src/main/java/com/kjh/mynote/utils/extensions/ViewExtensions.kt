package com.kjh.mynote.utils.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.kjh.mynote.ui.common.listener.OnThrottleClickListener

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 24..
 * Description: View 관련 확장 함수들.
 */

fun View.makeInVisible() {
    visibility = View.INVISIBLE
}

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun View.onThrottleClick(action: (v: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener))
}

fun View.setOnThrottleClickListener(listener: View.OnClickListener) {
    setOnClickListener(OnThrottleClickListener(listener))
}

fun AppCompatEditText.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

internal fun AppCompatTextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))
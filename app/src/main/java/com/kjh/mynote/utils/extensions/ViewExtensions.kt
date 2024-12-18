package com.kjh.mynote.utils.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.kjh.mynote.R
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

fun AppCompatEditText.showKeyboard() {
    requestFocus()
    performClick()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun AppCompatEditText.hideKeyboard() {
    this.clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

internal fun AppCompatTextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))

internal fun AppCompatImageView.setTint(@ColorRes color: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
}

internal fun View.setBackgroundRes(@DrawableRes res: Int) {
    background = ContextCompat.getDrawable(context, res)
}

fun AppCompatImageView.loadImage(url: String) {
    Glide.with(this.context)
        .load(url)
        .error(R.drawable.ic_launcher_foreground)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun AppCompatTextView.highlightText(fullText: String, wordToHighlight: String, isBold: Boolean = false) {
    val spannableString = SpannableString(fullText)
    val startIndex = fullText.indexOf(wordToHighlight, ignoreCase = true)

    if (startIndex != -1) {
        // 특정 단어의 시작 위치를 찾았을 때 색상을 변경
        spannableString.setSpan(
            ForegroundColorSpan(context.getColorCompat(R.color.purple)),
            startIndex,
            startIndex + wordToHighlight.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        if (isBold) {
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + wordToHighlight.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    text = spannableString
}

fun View.addClickAnimation() {
    setOnTouchListener { v, event ->
        if (v.isClickable) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).start()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
        }
        false
    }
}
package com.kjh.mynote.utils.extensions

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 14..
 * Description:
 */


internal fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable =
    requireNotNull(ContextCompat.getDrawable(this, drawable))

internal fun Context.getColorCompat(@ColorRes color: Int) =
    ContextCompat.getColor(this, color)


fun Context.getDisplaySize(): Rect {
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    if (Build.VERSION.SDK_INT < 30) {
        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)

        val rect = Rect()
        rect.left = 0
        rect.top = 0
        rect.right = size.x
        rect.bottom = size.y

        return rect
    }
    else {
        return windowManager.currentWindowMetrics.bounds
    }
}

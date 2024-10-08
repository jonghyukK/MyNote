package com.kjh.mynote.utils.extensions

import android.content.res.Resources
import androidx.core.content.ContextCompat

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 7..
 * Description:
 */

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}
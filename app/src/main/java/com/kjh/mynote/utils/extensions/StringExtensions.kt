package com.kjh.mynote.utils.extensions

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */


fun String?.ifNullOrEmpty(nextStr: String): String {
    return if (this.isNullOrEmpty()) {
        nextStr
    } else {
        this
    }
}
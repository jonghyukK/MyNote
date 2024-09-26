package com.example.data.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 26..
 * Description:
 */
sealed class Result<out T> {

    object Loading: Result<Nothing>()

    data class Success<out T>(val data: T?): Result<T>()

    data class Error(val msg: String?): Result<Nothing>()
}
package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
sealed class Result<out T> {

    object Loading: Result<Nothing>()

    data class Success<out T>(val data: T?): Result<T>()

    data class Error(val msg: String?): Result<Nothing>()
}
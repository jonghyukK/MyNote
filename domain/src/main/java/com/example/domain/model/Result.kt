package com.example.domain.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */
sealed class Result<out T> {

    data object Loading: Result<Nothing>()

    data class Success<out T>(val data: T?): Result<T>()

    data class Error(
        val msg: String?,
        val errorCode: Int = ERROR_CODE_DEFAULT
    ): Result<Nothing>()
}

const val ERROR_CODE_DEFAULT = 1000
const val ERROR_CODE_NULL = 9999
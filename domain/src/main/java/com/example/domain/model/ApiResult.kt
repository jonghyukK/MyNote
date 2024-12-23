package com.example.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 5..
 * Description:
 */

sealed interface ApiResult<out T> {
    data object Loading: ApiResult<Nothing>
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error<T>(val error: Throwable) : ApiResult<T>
}

inline fun <T, R> ApiResult<T>.getResult(
    loading: (ApiResult.Loading) -> R,
    success: (ApiResult.Success<T>) -> R,
    error: (ApiResult.Error<T>) -> R
): R = when (this) {
    is ApiResult.Loading -> loading(this)
    is ApiResult.Success -> success(this)
    is ApiResult.Error -> error(this)
}

inline fun <T> ApiResult<T>.onLoading(
    action: () -> Unit
): ApiResult<T> {
    if (this is ApiResult.Loading) action()
    return this
}

inline fun <T> ApiResult<T>.onSuccess(
    action: (T) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(
    action: (Throwable) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Error) action(error)
    return this
}

inline fun <T> safeApiCall(crossinline call: suspend () -> T): Flow<ApiResult<T>> = flow {
    emit(ApiResult.Loading)

    try {
        val data = call()
        emit(ApiResult.Success(data))
    } catch (e: Exception) {
        emit(ApiResult.Error(e))
    }
}

fun <T> Flow<T>.asResult(): Flow<ApiResult<T>> = map<T, ApiResult<T>> { ApiResult.Success(it) }
    .onStart { emit(ApiResult.Loading) }
    .catch { emit(ApiResult.Error(it)) }
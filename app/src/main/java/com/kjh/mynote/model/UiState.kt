package com.kjh.mynote.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
 */
sealed class UiState<out T: Any> {
    object Init: UiState<Nothing>()
    object Loading: UiState<Nothing>()

    data class Success<T: Any>(val data: T): UiState<T>()

    data class Error(val errorMsg: String): UiState<Nothing>()
}
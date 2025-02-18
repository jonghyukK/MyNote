package com.kjh.mynote.model

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
 */
sealed interface UiState<out T> {
    data object Loading: UiState<Nothing>
    data object Error: UiState<Nothing>
    data class Success<T>(val data: T): UiState<T>
}
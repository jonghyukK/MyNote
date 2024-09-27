package com.example.mynote.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 27..
 * Description:
 */

open class BaseViewModel: ViewModel() {

    private val _errorEvent = MutableSharedFlow<String?>()
    val errorEvent = _errorEvent.asSharedFlow()

    fun emitError(errorMsg: String?) {
        viewModelScope.launch {
            errorMsg?.let { _errorEvent.emit(errorMsg) }
        }
    }
}
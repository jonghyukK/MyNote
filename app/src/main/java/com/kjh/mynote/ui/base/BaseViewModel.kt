package com.kjh.mynote.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 27..
 * Description:
 */

abstract class BaseViewModel: ViewModel() {

    private val _showError: Channel<String> = Channel()
    val showError = _showError.receiveAsFlow()

    fun sendError(errorMsg: String?) {
        viewModelScope.launch {
            _showError.send(errorMsg ?: "Unexpected Error")
        }
    }
}
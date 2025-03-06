package com.kjh.mynote.ui_compose.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2025. 3. 1..
 * Description:
 */

interface UiState

interface UiEvent

interface UiSideEffect

abstract class BaseComposeViewModel<E: UiEvent, S: UiState, SE: UiSideEffect>: ViewModel() {

    private val initialState: S by lazy { createInitialState() }
    abstract fun createInitialState(): S

    private val event = Channel<E>()

    val state: StateFlow<S> = event.receiveAsFlow()
        .runningFold(initialState, ::reduceState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)

    private val _effect: Channel<SE> = Channel()
    val effect = _effect.receiveAsFlow()

    fun setEvent(event: E) {
        viewModelScope.launch { this@BaseComposeViewModel.event.send(event) }
    }

    protected abstract suspend fun reduceState(current: S, event: E): S

    abstract fun handleEvent(event: E)

    protected fun setEffect(builder: () -> SE) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }
}


//interface UiState
//
//interface UiEvent
//
//interface UiSideEffect
//
//abstract class BaseComposeViewModel<E: UiEvent, S: UiState, SE: UiSideEffect>: ViewModel() {
//
//    private val initialState: S by lazy { createInitialState() }
//    abstract fun createInitialState(): S
//
//    val currentState: S
//        get() = uiState.value
//
//    private val _uiState: MutableStateFlow<S> = MutableStateFlow(initialState)
//    val uiState = _uiState.asStateFlow()
//
//    private val _event: MutableSharedFlow<E> = MutableSharedFlow()
//    val event = _event.asSharedFlow()
//
//    private val _effect: Channel<SE> = Channel()
//    val effect = _effect.receiveAsFlow()
//
//    init {
//        subscribeEvents()
//    }
//
//    private fun subscribeEvents() {
//        viewModelScope.launch {
//            event.collect {
//                handleEvent(it)
//            }
//        }
//    }
//
//    abstract fun handleEvent(event: E)
//
//    fun setEvent(event: E) {
//        viewModelScope.launch { _event.emit(event) }
//    }
//
//    protected fun setState(reduce: S.() -> S) {
//        val newState = currentState.reduce()
//        _uiState.value = newState
//    }
//
//    protected fun setEffect(builder: () -> SE) {
//        val effectValue = builder()
//        viewModelScope.launch { _effect.send(effectValue) }
//    }
//}
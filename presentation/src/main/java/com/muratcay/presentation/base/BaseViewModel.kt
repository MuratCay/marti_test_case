package com.muratcay.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : IState> : ViewModel() {

    private val initialState: State by lazy { setInitialState() }

    abstract fun setInitialState(): State

    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    fun setState(state: State) {
        viewModelScope.launch { _state.emit(state) }
    }
}
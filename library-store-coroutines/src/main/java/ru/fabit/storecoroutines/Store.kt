package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<State, Action> {

    fun start()

    val state: Flow<State>

    val currentState: StateFlow<State>

    fun dispatchAction(action: Action)

    fun dispose()
}
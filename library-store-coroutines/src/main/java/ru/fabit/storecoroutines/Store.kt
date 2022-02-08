package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.Flow

interface Store<State, Action> {

    val state: Flow<State>

    val currentState: State

    fun dispatchAction(action: Action)

    fun dispose()
}
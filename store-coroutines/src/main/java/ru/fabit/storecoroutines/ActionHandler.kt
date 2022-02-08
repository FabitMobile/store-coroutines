package ru.fabit.storecoroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

open class ActionHandler<State, Action>(
    val requirement: (Action) -> Boolean,
    private val handler: suspend (State, Action) -> Unit,
    val dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    suspend operator fun invoke(state: State, action: Action) = handler(state, action)
}
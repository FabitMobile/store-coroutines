package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.Flow

open class BindActionSource<State, Action>(
    val requirement: (Action) -> Boolean,
    private val source: suspend (State, Action) -> Flow<Action>,
    private val error: (Throwable) -> Action = { t: Throwable -> throw t }
) {
    suspend operator fun invoke(state: State, action: Action) = source(state, action)

    operator fun invoke(throwable: Throwable) = error(throwable)
}
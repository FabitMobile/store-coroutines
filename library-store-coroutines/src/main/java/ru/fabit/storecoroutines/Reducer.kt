package ru.fabit.storecoroutines

interface Reducer<State, Action> {
    fun reduce(state: State, action: Action): State
}
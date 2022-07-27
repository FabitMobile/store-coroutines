package ru.fabit.storecoroutines

interface EventsReducer<State, Action> : Reducer<State, Action> {
    fun copy(state: State): State
}
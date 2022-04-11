package ru.fabit.storecoroutines

interface EventsReducer<State, Action> : Reducer<State, Action> {
    fun postReduce(state: State, action: Action): State {
        val cleared = copy(state)
        if (cleared is EventsState<*>)
            cleared.clearEvents()
        return cleared
    }

    fun copy(state: State): State
}
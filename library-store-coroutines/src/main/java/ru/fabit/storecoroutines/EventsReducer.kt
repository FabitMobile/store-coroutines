package ru.fabit.storecoroutines

interface EventsReducer<State, Action> : Reducer<State, Action> {
    fun preReduce(state: State, action: Action) {
        if (state is EventsState<*>)
            state.clearEvents()
    }
}
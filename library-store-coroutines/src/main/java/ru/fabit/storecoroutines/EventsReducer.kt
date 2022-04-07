package ru.fabit.storecoroutines

interface EventsReducer<State, Action> : Reducer<State, Action> {
    override fun reduce(state: State, action: Action): State {
        preReduce(state, action)

        return state
    }

    fun preReduce(state: State, action: Action) {
        if (state is EventsState<*>)
            state.clearEvents()
    }
}
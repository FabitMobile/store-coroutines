package ru.fabit.storecoroutines

class BaseReducer<State, Action> : Reducer<State, Action> {
    override fun reduce(state: State, action: Action): State {
        if (state is BaseState<*>)
            state.clearEvents()

        return state
    }
}
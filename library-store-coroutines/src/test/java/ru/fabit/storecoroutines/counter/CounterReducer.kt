package ru.fabit.storecoroutines.counter

import ru.fabit.storecoroutines.Reducer

class CounterReducer : Reducer<CounterState, CounterAction> {
    override fun reduce(state: CounterState, action: CounterAction): CounterState {
        return when (action) {
            is CounterAction.BootstrapAction -> state.copy(
                value = state.value + action.value
            )
            is CounterAction.Action -> state.copy(
                value = state.value + action.value
            )

            is CounterAction.BindAction -> state.copy(
                value = state.value + action.value
            )
            else -> state.copy()
        }
    }
}
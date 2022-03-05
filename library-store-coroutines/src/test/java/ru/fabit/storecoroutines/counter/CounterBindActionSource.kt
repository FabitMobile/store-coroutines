package ru.fabit.storecoroutines.counter

import kotlinx.coroutines.flow.flowOf
import ru.fabit.storecoroutines.BindActionSource

class CounterBindActionSource : BindActionSource<CounterState, CounterAction>(
    requirement = { action -> action is CounterAction.Action },
    source = { _, _ ->
        flowOf(CounterAction.BindAction(1))
    },
    error = { CounterAction.NoAction }
)
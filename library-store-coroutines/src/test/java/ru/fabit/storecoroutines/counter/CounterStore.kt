package ru.fabit.storecoroutines.counter

import ru.fabit.storecoroutines.*
import java.util.concurrent.CopyOnWriteArrayList


class CounterStore(
    currentState: CounterState,
    reducer: CounterReducer,
    errorHandler: ErrorHandler,
    bootStrapAction: CounterAction,
    actionHandlers: Iterable<ActionHandler<CounterState, CounterAction>> = CopyOnWriteArrayList(),
    actionSources: Iterable<ActionSource<CounterAction>> = CopyOnWriteArrayList(),
    bindActionSources: Iterable<BindActionSource<CounterState, CounterAction>> = CopyOnWriteArrayList(),
    sideEffects: Iterable<SideEffect<CounterState, CounterAction>> = CopyOnWriteArrayList()
) : BaseStore<CounterState, CounterAction>(
    currentState,
    reducer,
    errorHandler,
    bootStrapAction,
    sideEffects,
    bindActionSources,
    actionSources,
    actionHandlers
)
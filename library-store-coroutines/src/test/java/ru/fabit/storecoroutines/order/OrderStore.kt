package ru.fabit.storecoroutines.order

import ru.fabit.storecoroutines.*
import java.util.concurrent.CopyOnWriteArrayList


class OrderStore(
    currentState: OrderState,
    reducer: OrderReducer,
    errorHandler: ErrorHandler,
    bootStrapAction: OrderAction,
    actionHandlers: Iterable<ActionHandler<OrderState, OrderAction>> = CopyOnWriteArrayList(),
    actionSources: Iterable<ActionSource<OrderAction>> = CopyOnWriteArrayList(),
    bindActionSources: Iterable<BindActionSource<OrderState, OrderAction>> = CopyOnWriteArrayList(),
    sideEffects: Iterable<SideEffect<OrderState, OrderAction>> = CopyOnWriteArrayList()
) : BaseStore<OrderState, OrderAction>(
    currentState,
    reducer,
    errorHandler,
    bootStrapAction,
    sideEffects,
    bindActionSources,
    actionSources,
    actionHandlers
)
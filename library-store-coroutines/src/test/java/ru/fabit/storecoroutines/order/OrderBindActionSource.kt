package ru.fabit.storecoroutines.order

import kotlinx.coroutines.flow.flowOf
import ru.fabit.storecoroutines.BindActionSource

class OrderBindActionSource : BindActionSource<OrderState, OrderAction>(
    requirement = { action -> action is OrderAction.Action },
    source = { _, _ ->
        flowOf(OrderAction.NoAction)
    },
    error = { OrderAction.NoAction }
)
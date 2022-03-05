package ru.fabit.storecoroutines.order

import kotlinx.coroutines.flow.flow
import ru.fabit.storecoroutines.ActionSource

class OrderActionSource(private val delay: Long) : ActionSource<OrderAction>(
    source = {
        flow {

            repeat(10) {
                kotlinx.coroutines.delay(delay)
                emit(OrderAction.Action(it.toString()))
            }
        }
    },
    error = {
        OrderAction.NoAction
    }
)
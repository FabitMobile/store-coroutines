package ru.fabit.storecoroutines.counter

import kotlinx.coroutines.flow.flow
import ru.fabit.storecoroutines.ActionSource

class CounterActionSource(
    private val repeat: Int,
    private val delay: Long
) : ActionSource<CounterAction>(
    source = {
        flow {
            repeat(repeat) {
                kotlinx.coroutines.delay(delay)
                emit(CounterAction.Action(1))
            }
        }
    },
    error = {
        CounterAction.NoAction
    }
)
package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.flow

class TestActionSource : ActionSource<TestAction>(
    source = {
        flow {
            var i = 0
            emit(TestAction.Action("TestActionSource, $i"))
            while (true) {
                i++
                kotlinx.coroutines.delay(1000)
                emit(TestAction.Action("TestActionSource, $i"))

            }
        }
    },
    error = {
        TestAction.NoAction
    }
)
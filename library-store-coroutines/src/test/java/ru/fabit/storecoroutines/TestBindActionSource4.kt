package ru.fabit.storecoroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class TestBindActionSource4 : BindActionSource<TestState, TestAction>(
    requirement = { action -> action is TestAction.BindAction4 },
    source = { _, action ->
        action as TestAction.BindAction4
        flow {
            emit(TestAction.Action(action.value))
            delay(5000)
            emit(TestAction.Action(action.value))
        }
    },
    error = { TestAction.BindAction("TestBindActionSource4") }
)
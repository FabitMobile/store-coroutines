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
            //одиноковые значения не обрабатываются
            emit(TestAction.Action(action.value + "2"))
        }
    },
    error = { TestAction.BindAction("TestBindActionSource4") }
)
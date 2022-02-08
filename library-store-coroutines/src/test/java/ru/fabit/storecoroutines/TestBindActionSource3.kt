package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.flowOf

class TestBindActionSource3 : BindActionSource<TestState, TestAction>(
    requirement = { action -> action is TestAction.Action3 },
    source = { _, _ ->
        flowOf(TestAction.BindAction3("TestBindActionSource3"))
    },
    error = { TestAction.BindAction3("TestBindActionSource3") }
)
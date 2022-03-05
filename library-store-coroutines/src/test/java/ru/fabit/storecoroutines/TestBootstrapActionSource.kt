package ru.fabit.storecoroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf

class TestBootstrapActionSource : BindActionSource<TestState, TestAction>(
    requirement = { action -> action is TestAction.BootstrapAction },
    source = { _, _ ->
        delay(100)
        flowOf(TestAction.NoAction)
    },
    error = { TestAction.BindAction("TestBindActionSource") }
)
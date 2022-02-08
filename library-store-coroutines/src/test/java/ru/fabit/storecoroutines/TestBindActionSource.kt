package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.flowOf

class TestBindActionSource : BindActionSource<TestState, TestAction>(
    requirement = { action -> action is TestAction.Action },
    source = { _, _ ->
        flowOf(TestAction.BindAction("TestBindActionSource"))
    },
    error = { TestAction.BindAction("TestBindActionSource") }
)
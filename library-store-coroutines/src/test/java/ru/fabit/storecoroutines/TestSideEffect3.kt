package ru.fabit.storecoroutines

class TestSideEffect3 : SideEffect<TestState, TestAction>(
    requirement = { _, action -> action is TestAction.BindAction3 },
    effect = { _, _ ->
        TestAction.SideAction3("TestSideEffect3")
    },
    error = { TestAction.SideAction3("TestSideEffect3") }
)
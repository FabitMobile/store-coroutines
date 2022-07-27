package ru.fabit.storecoroutines

data class TestState(
    val value: String
) : EventsState<TestEvent>()
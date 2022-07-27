package ru.fabit.storecoroutines

sealed class TestEvent {
    object Event : TestEvent() {
        override fun toString(): String {
            return "TestEvent.Event"
        }
    }
}
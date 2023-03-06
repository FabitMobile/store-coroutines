package ru.fabit.storecoroutines

sealed class TestEvent {
    object Event : TestEvent() {
        override fun toString(): String {
            return "TestEvent.Event"
        }
    }
    object Event2 : TestEvent() {
        override fun toString(): String {
            return "TestEvent.Event2"
        }
    }
}
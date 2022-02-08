package ru.fabit.storecoroutines

interface ErrorHandler {
    fun handle(t: Throwable)
}
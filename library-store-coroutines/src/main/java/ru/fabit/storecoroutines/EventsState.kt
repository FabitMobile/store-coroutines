package ru.fabit.storecoroutines

open class EventsState<Event>(
    private val events: MutableList<Event> = mutableListOf()
) {
    fun events(): List<Event> {
        return events
    }

    fun addEvent(event: Event) {
        events.add(event)
    }

    fun clearEvent(event: Event) {
        events.remove(event)
    }

    fun clearEvents() {
        events.clear()
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventsState<*>

        if (events != other.events) return false

        return true
    }

    override fun hashCode(): Int {
        return events.hashCode()
    }
}
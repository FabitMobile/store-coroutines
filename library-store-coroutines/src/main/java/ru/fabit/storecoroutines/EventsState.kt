package ru.fabit.storecoroutines

open class EventsState<Event>(
    private val events: MutableList<Event> = mutableListOf()
) {
    fun events(): List<Event> {
        return events
    }

    fun addEvent(event: Event): EventsState<Event> {
        events.add(event)
        return this
    }

    fun addEvents(events: List<Event>): EventsState<Event> {
        events.forEach {
            this.events.add(it)
        }
        return this
    }

    fun clearEvent(event: Event): EventsState<Event> {
        events.remove(event)
        return this
    }

    fun clearEvents(): EventsState<Event> {
        events.clear()
        return this
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
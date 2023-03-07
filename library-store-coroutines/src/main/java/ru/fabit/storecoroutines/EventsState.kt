package ru.fabit.storecoroutines

abstract class EventsState<Event>(
    val events: MutableList<Event> = mutableListOf()
) {
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

    @Suppress("UNCHECKED_CAST")
    fun mergeEvents(otherState: Any?) {
        val newEvents = events.toList()
        events.clear()
        events.addAll((otherState as EventsState<Event>).events)
        events.addAll(newEvents)
    }

    fun clearEvent(event: Event) {
        events.remove(event)
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
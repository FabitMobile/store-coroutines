package ru.fabit.storecoroutines

class TestReducer : EventsReducer<TestState, TestAction> {
    override fun reduce(state: TestState, action: TestAction): TestState {
        return when (action) {
            is TestAction.NoAction -> state.copy(
                value = "no action"
            )
            is TestAction.BootstrapAction -> state.copy(
                value = "bootstrap action"
            ).apply {
                addEvent(TestEvent.Event)
            }
            is TestAction.Action -> state.copy(
                value = action.value
            )
            is TestAction.Action2 -> state.copy(
                value = action.value
            )
            is TestAction.Action3 -> state.copy(
                value = action.value
            )
            is TestAction.BindAction -> state.copy(
                value = action.value
            )
            is TestAction.BindAction2 -> state.copy(
                value = action.value
            )
            is TestAction.BindAction3 -> state.copy(
                value = action.value
            )
            is TestAction.BindAction4 -> state.copy(
                value = action.value
            )
            is TestAction.SideAction -> state.copy(
                value = action.value
            )
            is TestAction.SideAction2 -> state.copy(
                value = action.value
            )
            is TestAction.SideAction3 -> state.copy(
                value = action.value
            )
            is TestAction.EventAction -> state.copy(
                value = "event action"
            ).apply {
                addEvent(TestEvent.Event2)
            }
            is TestAction.OrderEventAction -> state.copy(
                value = "order event action ${action.order}"
            ).apply {
                addEvent(TestEvent.OrderEvent(action.order))
            }

            else -> state.copy()
        }
    }

    override fun copy(state: TestState) = state.copy()
}
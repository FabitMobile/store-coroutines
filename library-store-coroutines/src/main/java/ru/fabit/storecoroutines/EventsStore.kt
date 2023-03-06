package ru.fabit.storecoroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import java.util.concurrent.CopyOnWriteArrayList

abstract class EventsStore<State : EventsState<Event>, Action, Event>(
    startState: State,
    private val reducer: EventsReducer<State, Action>,
    errorHandler: ErrorHandler,
    private val bootstrapAction: Action? = null,
    sideEffects: Iterable<SideEffect<State, Action>> = CopyOnWriteArrayList(),
    bindActionSources: Iterable<BindActionSource<State, Action>> = CopyOnWriteArrayList(),
    actionSources: Iterable<ActionSource<Action>> = CopyOnWriteArrayList(),
    actionHandlers: Iterable<ActionHandler<State, Action>> = CopyOnWriteArrayList()
) : BaseStore<State, Action>(
    startState,
    reducer,
    errorHandler,
    bootstrapAction,
    sideEffects,
    bindActionSources,
    actionSources,
    actionHandlers
) {
    override val state: Flow<State>
        get() {
            _state.tryEmit(currentState.value)
            return _state.onEach {
                _currentState.value = reducer.copy(currentState.value)
            }
        }

    override suspend fun handleActions() {
        actions.onSubscription {
            bootstrapAction?.let {
                emit(it)
            }
        }.collect { action ->
            val state = reducer.reduce(currentState.value, action)
            state.mergeEvents(currentState.value)
            _state.emit(state)
            _currentState.value = state
            dispatchSideEffect(state, action)
            dispatchActionHandler(state, action)
            dispatchBindActionSource(state, action)
        }
    }
}

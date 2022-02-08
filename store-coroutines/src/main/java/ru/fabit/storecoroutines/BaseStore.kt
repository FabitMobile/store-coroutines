package ru.fabit.storecoroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.CopyOnWriteArrayList

abstract class BaseStore<State, Action>(
    currentState: State,
    private val reducer: Reducer<State, Action>,
    private val errorHandler: ErrorHandler,
    bootstrapAction: Action? = null,
    private val sideEffects: Iterable<SideEffect<State, Action>> = CopyOnWriteArrayList(),
    private val bindActionSources: Iterable<BindActionSource<State, Action>> = CopyOnWriteArrayList(),
    private val actionSources: Iterable<ActionSource<Action>> = CopyOnWriteArrayList(),
    private val actionHandlers: Iterable<ActionHandler<State, Action>> = CopyOnWriteArrayList()
) : Store<State, Action> {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _actions = MutableSharedFlow<Action>()
    private val actions = _actions.asSharedFlow()

    private val _state = MutableStateFlow(currentState)
    override val state = _state.asStateFlow()

    private var _currentState: State = currentState
    override val currentState: State
        get() = _currentState

    init {
        scope.launch(Dispatchers.IO) {
            handleActions()
        }
        scope.launch(Dispatchers.IO) {
            dispatchActionSource()
        }
        bootstrapAction?.let {
            dispatchAction(it)
        }
    }

    override fun dispatchAction(action: Action) {
        scope.launch(Dispatchers.IO) {
            _actions.emit(action)
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    private suspend inline fun handleActions() {
        actions.collect { action ->
            val state = reducer.reduce(currentState, action)
            _state.emit(state)
            _currentState = state
            scope.launch(Dispatchers.IO) {
                dispatchSideEffect(state, action)
            }
            scope.launch(Dispatchers.IO) {
                dispatchActionHandler(state, action)
            }
            scope.launch(Dispatchers.IO) {
                dispatchBindActionSource(state, action)
            }
        }
    }

    private suspend inline fun dispatchSideEffect(state: State, action: Action) {
        sideEffects.filter { sideEffect ->
            sideEffect.requirement(state, action)
        }.forEach { sideEffect ->
            try {
                _actions.emit(sideEffect(state, action))
            } catch (t: Throwable) {
                errorHandler.handle(t)
                _actions.emit(sideEffect(t))
            }
        }
    }

    private suspend inline fun dispatchActionSource() {
        actionSources.map { actionSource ->
            try {
                actionSource().catch {
                    errorHandler.handle(it)
                    emit(actionSource(it))
                }
            } catch (t: Throwable) {
                errorHandler.handle(t)
                flowOf(actionSource(t))
            }
        }.merge().collect {
            _actions.emit(it)
        }
    }

    private suspend inline fun dispatchBindActionSource(state: State, action: Action) {
        bindActionSources.filter { bindActionSource ->
            bindActionSource.requirement(action)
        }.map { bindActionSource ->
            try {
                bindActionSource(state, action).catch {
                    errorHandler.handle(it)
                    emit(bindActionSource(it))
                }
            } catch (t: Throwable) {
                errorHandler.handle(t)
                flowOf(bindActionSource(t))
            }
        }.merge().collect {
            _actions.emit(it)
        }
    }

    private suspend inline fun dispatchActionHandler(state: State, action: Action) {
        actionHandlers.filter { actionHandler ->
            actionHandler.requirement(action)
        }.forEach { actionHandler ->
            try {
                withContext(actionHandler.dispatcher) {
                    actionHandler(state, action)
                }
            } catch (t: Throwable) {
                errorHandler.handle(t)
            }
        }
    }
}

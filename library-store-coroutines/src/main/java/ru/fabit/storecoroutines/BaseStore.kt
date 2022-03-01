package ru.fabit.storecoroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
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

    private var sideEffectsJobs: MutableMap<String, Job?> = mutableMapOf()
    private var actionHandlersJobs: MutableMap<String, Job?> = mutableMapOf()
    private var bindActionSourcesJobs: MutableMap<String, Job?> = mutableMapOf()
    private var actionSourcesJobs: MutableMap<String, Job?> = mutableMapOf()

    private val _actions = MutableSharedFlow<Action>()
    private val actions = _actions.asSharedFlow()

    private val _state = MutableSharedFlow<State>(replay = 1)
    override val state: SharedFlow<State>
        get() {
            _state.tryEmit(currentState)
            return _state.asSharedFlow()
        }
    private var _currentState: State = currentState
    override val currentState: State
        get() = _currentState

    init {
        scope.launch {
            handleActions()
        }
        scope.launch {
            dispatchActionSource()
        }
        bootstrapAction?.let {
            dispatchAction(it)
        }
    }

    override fun dispatchAction(action: Action) {
        scope.launch {
            _actions.emit(action)
        }
    }

    override fun dispose() {
        scope.cancel()
        sideEffectsJobs.clear()
        actionHandlersJobs.clear()
        bindActionSourcesJobs.clear()
        actionSourcesJobs.clear()
    }

    private suspend fun handleActions() {
        actions.collect { action ->
            val state = reducer.reduce(currentState, action)
            _state.emit(state)
            _currentState = state
            dispatchSideEffect(state, action)
            dispatchActionHandler(state, action)
            dispatchBindActionSource(state, action)
        }
    }

    private fun dispatchSideEffect(state: State, action: Action) {
        sideEffects.filter { sideEffect ->
            sideEffect.requirement(state, action)
        }.forEach { sideEffect ->
            sideEffectsJobs.start(sideEffect::class.java.simpleName) {
                scope.launch {
                    try {
                        _actions.emit(sideEffect(state, action))
                    } catch (t: Throwable) {
                        t.handleCancellationException {
                            errorHandler.handle(t)
                            _actions.emit(sideEffect(t))
                        }
                    }
                }
            }
        }
    }

    private fun dispatchActionSource() {
        actionSources.map { actionSource ->
            actionSourcesJobs.start(actionSource::class.java.simpleName) {
                scope.launch {
                    try {
                        actionSource().catch {
                            it.handleCancellationException {
                                errorHandler.handle(it)
                                emit(actionSource(it))
                            }
                        }.collect {
                            _actions.emit(it)
                        }
                    } catch (t: Throwable) {
                        t.handleCancellationException {
                            errorHandler.handle(t)
                            _actions.emit(actionSource(t))
                        }
                    }
                }
            }
        }
    }

    private fun dispatchBindActionSource(state: State, action: Action) {
        bindActionSources.filter { bindActionSource ->
            bindActionSource.requirement(action)
        }.map { bindActionSource ->
            bindActionSourcesJobs.start(bindActionSource::class.java.simpleName) {
                scope.launch {
                    try {
                        bindActionSource(state, action).catch {
                            it.handleCancellationException {
                                errorHandler.handle(it)
                                emit(bindActionSource(it))
                            }
                        }
                            .collect {
                                _actions.emit(it)
                            }
                    } catch (t: Throwable) {
                        t.handleCancellationException {
                            errorHandler.handle(t)
                            _actions.emit(bindActionSource(t))
                        }
                    }
                }
            }
        }
    }

    private fun dispatchActionHandler(state: State, action: Action) {
        actionHandlers.filter { actionHandler ->
            actionHandler.requirement(action)
        }.forEach { actionHandler ->
            actionHandlersJobs.start(actionHandler::class.java.simpleName) {
                scope.launch(actionHandler.dispatcher) {
                    try {
                        actionHandler(state, action)
                    } catch (t: Throwable) {
                        t.handleCancellationException { errorHandler.handle(t) }
                    }
                }
            }
        }
    }

    private suspend fun Throwable.handleCancellationException(func: suspend () -> Unit) {
        if (this !is CancellationException) {
            func()
        }
    }

    private fun MutableMap<String, Job?>.start(key: String, func: () -> Job) {
        this[key]?.cancel()
        val job = func()
        this[key] = job
    }
}

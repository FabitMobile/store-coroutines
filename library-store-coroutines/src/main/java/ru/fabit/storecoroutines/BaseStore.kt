package ru.fabit.storecoroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.util.concurrent.CopyOnWriteArrayList

abstract class BaseStore<State, Action>(
    startState: State,
    private val reducer: Reducer<State, Action>,
    private val errorHandler: ErrorHandler,
    private val bootstrapAction: Action? = null,
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
    private var awaitSubscriptionJob: Job? = null

    protected val _actions = MutableSharedFlow<Action>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    protected val actions = _actions.asSharedFlow()

    protected val _state = MutableSharedFlow<State>(replay = 1)
    override val state: Flow<State>
        get() {
            _state.tryEmit(currentState.value)
            return _state
        }

    protected var _currentState: MutableStateFlow<State> = MutableStateFlow(startState)
    override val currentState: StateFlow<State>
        get() = _currentState

    override fun start() {
        awaitSubscription()
        scope.launch {
            handleActions()
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
        awaitSubscriptionJob?.cancel()
    }

    private fun awaitSubscription() {
        awaitSubscriptionJob = scope.launch {
            _actions.subscriptionCount
                .filter { it > 0 }
                .take(1)
                .collect { count ->
                    if (count > 0) {
                        dispatchActionSource()
                    }
                }
        }
    }

    protected open suspend fun handleActions() {
        actions.onSubscription {
            bootstrapAction?.let {
                emit(it)
            }
        }.collect { action ->
            val state = reducer.reduce(currentState.value, action)
            _state.emit(state)
            _currentState.value = state
            dispatchSideEffect(state, action)
            dispatchActionHandler(state, action)
            dispatchBindActionSource(state, action)
        }
    }

    protected open fun dispatchSideEffect(state: State, action: Action) {
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

    protected open fun dispatchActionSource() {
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

    protected open fun dispatchBindActionSource(state: State, action: Action) {
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

    protected open fun dispatchActionHandler(state: State, action: Action) {
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

    private suspend fun Throwable.handleCancellationException(action: suspend () -> Unit) {
        if (this !is CancellationException) {
            action()
        }
    }

    private fun MutableMap<String, Job?>.start(key: String, action: () -> Job) {
        this[key]?.cancel()
        val job = action()
        this[key] = job
    }
}

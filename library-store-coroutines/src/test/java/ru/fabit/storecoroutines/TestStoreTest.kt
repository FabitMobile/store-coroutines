package ru.fabit.storecoroutines

import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import ru.fabit.storecoroutines.counter.*
import ru.fabit.storecoroutines.order.*
import java.util.concurrent.CopyOnWriteArrayList

class TestStoreTest {

    private val errorHandler =
        object : ErrorHandler {
            override fun handle(t: Throwable) {
                println(t)
            }
        }

    private fun store() = TestStore(
        TestState("init"),
        TestReducer(),
        errorHandler,
        TestAction.BootstrapAction,
        actionSources = CopyOnWriteArrayList(
            listOf(
                TestActionSource(),
                TestActionSource2(),
                TestActionSource3()
            )
        ),
        bindActionSources = CopyOnWriteArrayList(
            listOf(
                TestBootstrapActionSource(),
                TestBindActionSource(),
                TestBindActionSource2(),
                TestBindActionSource3(),
                TestBindActionSource4()
            )
        ),
        sideEffects = CopyOnWriteArrayList(
            listOf(
                TestSideEffect(),
                TestSideEffect2(),
                TestSideEffect3()
            )
        )
    )

    private fun storeMini() = TestStore(
        TestState("init"),
        TestReducer(),
        errorHandler,
        TestAction.BootstrapAction,
        bindActionSources = CopyOnWriteArrayList(
            listOf(
                TestBindActionSource4()
            )
        )
    )

    private fun storeCounter(repeat: Int, delay: Long) = CounterStore(
        currentState = CounterState(1),
        reducer = CounterReducer(),
        errorHandler = errorHandler,
        bootStrapAction = CounterAction.BootstrapAction(1),
        bindActionSources = CopyOnWriteArrayList(
            listOf(
                CounterBindActionSource()
            )
        ),
        actionSources = CopyOnWriteArrayList(
            listOf(
                CounterActionSource(repeat, delay)
            )
        )
    )

    private fun storeOrder(delay: Long) = OrderStore(
        currentState = OrderState("_"),
        reducer = OrderReducer(),
        errorHandler = errorHandler,
        bootStrapAction = OrderAction.NoAction,
        bindActionSources = CopyOnWriteArrayList(listOf(OrderBindActionSource())),
        actionSources = CopyOnWriteArrayList(
            listOf(
                OrderActionSource(delay)
            )
        )
    )

    @Test
    fun test() = runBlocking {
        val states = mutableListOf<String>()
        val store = store()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            store.state.collect { state ->
                states.add(state.value)
            }
        }
        delay(100)
        store.dispatchAction(TestAction.NoAction)
        delay(100)
        states.remove("init")
        states.remove("bootstrap action")
        Assert.assertEquals(
            listOf(
                "TestActionSource, 0",
                "TestActionSource2",
                "TestActionSource3, 0",
                "TestBindActionSource",
                "TestBindActionSource2",
                "TestBindActionSource3",
                "TestSideEffect",
                "TestSideEffect2",
                "TestSideEffect3",
                "no action",
                "no action"
            ).sorted(), states.sorted()
        )
        job.cancel()
        store.dispose()
    }

    @Test
    fun test2() = runBlocking {
        val states = mutableListOf<String>()
        val store = store()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            store.state.collect { state ->
                // println(state.value)
                states.add(state.value)
            }
        }
        delay(3000)
        store.dispatchAction(TestAction.NoAction)
        delay(5_000)
        states.remove("init")
        states.remove("bootstrap action")
        Assert.assertEquals(
            listOf(
                "TestActionSource, 0",
                "TestActionSource, 1",
                "TestActionSource, 2",
                "TestActionSource, 3",
                "TestActionSource, 4",
                "TestActionSource, 5",
                "TestActionSource, 6",
                "TestActionSource, 7",
                "TestActionSource3, 0",
                "TestActionSource3, 1",
                "TestActionSource3, 2",
                "TestActionSource3, 3",
                "TestActionSource2",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource",
                "TestBindActionSource2",
                "TestBindActionSource3",
                "TestBindActionSource3",
                "TestBindActionSource3",
                "TestBindActionSource3",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect",
                "TestSideEffect2",
                "TestSideEffect3",
                "TestSideEffect3",
                "TestSideEffect3",
                "TestSideEffect3",
                "no action",
                "no action"
            ).sorted(),
            states.sorted()
        )
        store.dispose()
        job.cancel()
    }

    @Test
    fun test3() = runBlocking {
        val states = mutableListOf<String>()
        val store = storeMini()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            store.state.collect { state ->
                states.add(state.value)
            }
        }
        delay(100)
        store.dispatchAction(TestAction.BindAction4("1"))
        delay(3_000)
        store.dispatchAction(TestAction.BindAction4("2"))
        delay(6_000)
        states.remove("init")
        states.remove("bootstrap action")
        Assert.assertEquals(
            listOf(
                "1",
                "BindActionSource4Completed",
                "BindActionSource4Completed",
                "2",
                "delayBindActionSource4Completed"
            ).sorted(),
            states.sorted()
        )
        store.dispose()
        job.cancel()
    }

    @Test
    fun `increment_with_delay_test`() = runBlocking {
        var finishState = 0
        val store = storeCounter(repeat = 10, delay = 100)
        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            store.state.collect { state ->
                finishState = state.value
            }
        }
        delay(2_000)
        Assert.assertEquals(22, finishState)
        store.dispose()
        job.cancel()
    }

    @Test
    fun `increment_without_delay_test`() = runBlocking {
        var finishState = 0
        val store = storeCounter(repeat = 100, delay = 0)
        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            store.state.collect { state ->
                finishState = state.value
            }
        }
        delay(5_000)
        Assert.assertTrue(finishState > 102)
        store.dispose()
        job.cancel()
    }

    @Test
    fun `check_order`() = runBlocking {
        var finishState = ""
        val store = storeOrder(delay = 0)
        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            store.state.collect { state ->
                finishState = state.value
            }
        }
        delay(2_000)
        Assert.assertEquals("_0123456789", finishState)
        store.dispose()
        job.cancel()
    }
}
package ru.fabit.storecoroutines

import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
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

    @Test
    fun test() = runBlocking {
        val actions = mutableListOf<String>()
        val store = store()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            store.state.collect { state ->
                println(state.value)
                actions.add(state.value)
            }
        }
        delay(100)
        store.dispatchAction(TestAction.NoAction)
        delay(100)
        Assert.assertEquals(
            listOf(
                "init",
                "bootstrap action",
                "TestActionSource, 0",
                "TestActionSource2",
                "TestActionSource3, 0",
                "TestBindActionSource",
                "TestBindActionSource2",
                "TestBindActionSource3",
                "TestSideEffect",
                "TestSideEffect2",
                "TestSideEffect3",
                "no action"
            ).sorted(), actions.sorted()
        )
        job.cancel()
        store.dispose()
    }

    @Test
    fun test2() = runBlocking {
        val actions = mutableListOf<String>()
        val store = store()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            store.state.collect { state ->
                println(state.value)
                actions.add(state.value)
            }
        }
        delay(3000)
        store.dispatchAction(TestAction.NoAction)
        delay(5_000)
        Assert.assertEquals(
            listOf(
                "bootstrap action",
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
                "init"
            ).sorted(),
            actions.sorted()
        )
        store.dispose()
        job.cancel()
    }

    @Test
    fun test3() = runBlocking {
        val actions = mutableListOf<String>()
        val store = storeMini()
        val job = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            store.state.collect { state ->
                println(state.value)
                actions.add(state.value)
            }
        }
        delay(100)
        store.dispatchAction(TestAction.BindAction4("1"))
        delay(3_000)
        store.dispatchAction(TestAction.BindAction4("2"))
        delay(6_000)
        Assert.assertEquals(
            listOf("bootstrap action", "init", "1", "BindActionSource4Completed", "BindActionSource4Completed", "2", "delayBindActionSource4Completed").sorted(), actions.sorted()
        )
        store.dispose()
        job.cancel()
    }
}
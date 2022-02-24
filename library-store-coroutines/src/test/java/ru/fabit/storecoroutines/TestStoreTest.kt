package ru.fabit.storecoroutines

import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

class TestStoreTest {
    private val errorHandler = object : ErrorHandler {
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

    private var actions = CopyOnWriteArrayList<String>()

    @Test
    fun test() = runBlocking {
        val store = store()

        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            store.state.collect { state ->
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
        actions.clear()
    }

    @Test
    fun test2() = runBlocking {
        val store = store()

        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            store.state.collect { state ->
                println(state.value)
                actions.add(state.value)
            }
        }
        delay(3000)
        store.dispatchAction(TestAction.NoAction)
        delay(60000)
        job.cancel()
        actions.clear()
    }

    @Test
    fun test3() = runBlocking {
        val store = storeMini()

        val job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
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
            listOf("bootstrap action", "init", "1", "2", "22").sorted(), actions.sorted()
        )
        job.cancel()
        actions.clear()
    }

    @Test
    fun `test x25`() = runBlocking {
        var i = 1
        repeat(25) {
            println("try ${i++}")
            test()
            delay(100)
        }
    }
}
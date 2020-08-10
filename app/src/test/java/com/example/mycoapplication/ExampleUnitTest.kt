package com.example.mycoapplication

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.Executors

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)

        val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

        val job = GlobalScope.launch(dispatcher) {
            runBlocking {
                repeat(10) { // 启动大量的协程
                    launch {
                        delay(1000L)
                        println(" ${Thread.currentThread().id} ${System.currentTimeMillis()}")
                    }
                }
            }
        }
    }
}

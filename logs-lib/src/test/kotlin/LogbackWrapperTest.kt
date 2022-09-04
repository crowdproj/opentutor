package com.gitlab.sszuev.flashcards.logslib

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

internal class LogbackWrapperTest {

    companion object {
        private const val logId = "test-log"
        private fun logWithError(failBlock: suspend () -> Unit): ByteArrayOutputStream {
            val out = standardOut()
            Assertions.assertThrowsExactly(TestException::class.java) {
                runBlocking {
                    logger(this::class.java).withLogging(logId, block = failBlock)
                }
            }
            return out
        }

        private fun logWithSuccess(successBlock: suspend () -> Unit): ByteArrayOutputStream {
            val out = standardOut()
            runBlocking {
                logger(this::class.java).withLogging(logId, block = successBlock)
            }
            return out
        }

        private fun standardOut(): ByteArrayOutputStream {
            return ByteArrayOutputStream().apply {
                System.setOut(PrintStream(this))
            }
        }
    }

    @Test
    fun `test with logging success block`() {
        val execOut = logWithSuccess {
            println("TEST")
        }
        val lines = execOut.toString(StandardCharsets.UTF_8).lines()
        Assertions.assertEquals(4, lines.size)
        Assertions.assertTrue(lines[0].contains(":::Start $logId"))
        Assertions.assertTrue(lines[2].contains(":::End $logId"))
        Assertions.assertTrue(lines[0].contains(" INFO "))
        Assertions.assertTrue(lines[2].contains(" INFO "))
        Assertions.assertEquals("TEST", lines[1])
        Assertions.assertEquals("", lines[3])
    }

    @Test
    fun `test with logging error block`() {
        val execOut = logWithError {
            throw TestException()
        }
        val lines = execOut.toString(StandardCharsets.UTF_8).lines()

        Assertions.assertEquals(4, lines.size)
        Assertions.assertTrue(lines[0].contains(":::Start $logId"))
        Assertions.assertTrue(lines[1].contains(":::Fail $logId"))
        Assertions.assertTrue(lines[0].contains(" INFO "))
        Assertions.assertTrue(lines[1].contains(" ERROR "))
        Assertions.assertTrue(lines[2].endsWith("test-exception"))
        Assertions.assertEquals("", lines[3])
    }

    class TestException : RuntimeException("test-exception") {
        override fun getStackTrace(): Array<StackTraceElement?> {
            return emptyArray()
        }
    }
}
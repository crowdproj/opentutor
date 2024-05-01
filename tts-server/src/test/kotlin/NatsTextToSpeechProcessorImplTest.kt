package com.gitlab.sszuev.flashcards.speaker

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.nats.client.Connection
import io.nats.client.Nats
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers
internal class NatsTextToSpeechProcessorImplTest {
    companion object {
        @Container
        private val natsContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("nats:latest"))
            .withExposedPorts(4222)

        @JvmStatic
        @AfterAll
        fun shutdown() {
            natsContainer.stop()
        }
    }

    private lateinit var connection: Connection
    private lateinit var connectionUrl: String

    @BeforeEach
    internal fun setUp() {
        connectionUrl = "nats://" + natsContainer.host + ":" + natsContainer.getMappedPort(4222)
        connection = Nats.connect(connectionUrl)
        Assumptions.assumeTrue(connection.status == Connection.Status.CONNECTED)
    }

    @AfterEach
    internal fun tearDown() {
        connection.close()
    }

    @Test
    fun `test send message success`() = runBlocking {
        val requestId = "qqq"
        val responseBody = ByteArray(42) { 42 }
        val service = mockk<TextToSpeechService>()
        coEvery {
            service.containsResource(requestId)
        } returns true
        coEvery {
            service.getResource(requestId)
        } returns responseBody
        val processor = NatsTextToSpeechProcessorImpl(
            service = service,
            topic = "XXX",
            group = "QQQ",
            connectionUrl = connectionUrl,
        )

        TextToSpeechController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }
        val answer =
            connection.request("XXX", requestId.toByteArray(Charsets.UTF_8), Duration.of(42, ChronoUnit.SECONDS))
        Assertions.assertArrayEquals(
            responseBody,
            answer.data
        ) { "wrong data: ${answer.data.toString(Charsets.UTF_8)}" }

        coVerify(exactly = 1) {
            service.containsResource(requestId)
        }
        coVerify(exactly = 1) {
            service.getResource(requestId)
        }
        processor.close()
    }

    @Test
    fun `test send message error`() = runBlocking {
        val requestId = "xxx"
        val service = mockk<TextToSpeechService>()
        coEvery {
            service.containsResource(requestId)
        } returns true
        coEvery {
            service.getResource(requestId)
        } throws RuntimeException("Expected error")
        val processor = NatsTextToSpeechProcessorImpl(
            service = service,
            topic = "XXX",
            group = "QQQ",
            connectionUrl = connectionUrl,
        )

        TextToSpeechController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }
        val answer =
            connection.request("XXX", requestId.toByteArray(Charsets.UTF_8), Duration.of(42, ChronoUnit.SECONDS))
        val res = answer.data.toString(Charsets.UTF_8)
        Assertions.assertTrue(res.startsWith("e:java.lang.RuntimeException: Expected error"))

        coVerify(exactly = 1) {
            service.containsResource(requestId)
        }
        coVerify(exactly = 1) {
            service.getResource(requestId)
        }
        processor.close()
    }
}
package com.gitlab.sszuev.flashcards.speaker

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSOperation
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.ttsContextFromByteArray
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
internal class NatsTTSServerProcessorImplTest {
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
        val testWord1 = "rrr"
        val testLang1 = "ttt"
        val testResponseBody1 = ByteArray(42) { 42 }
        val testRequestId1 = "$testLang1:$testWord1"
        val testResponseEntity1 = ResourceEntity(resourceId = TTSResourceId(testRequestId1), data = testResponseBody1)

        val testWord2 = "yyy"
        val testLang2 = "nnn"
        val testResponseBody2 = ByteArray(42) { 21 }
        val testRequestId2 = "$testLang2:$testWord2"
        val testResponseEntity2 = ResourceEntity(resourceId = TTSResourceId(testRequestId2), data = testResponseBody2)

        val service = mockk<TextToSpeechService>()
        coEvery {
            service.containsResource(testRequestId1)
        } returns true
        coEvery {
            service.getResource(testRequestId1)
        } returns testResponseBody1
        coEvery {
            service.containsResource(testRequestId2)
        } returns true
        coEvery {
            service.getResource(testRequestId2)
        } returns testResponseBody2

        val processor = NatsTTSServerProcessorImpl(
            service = service,
            topic = "XXX",
            group = "QQQ",
            connectionUrl = connectionUrl,
        )

        TTSServerController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }

        val context1 = TTSContext(operation = TTSOperation.GET_RESOURCE, requestAppAuthId = AppAuthId("uuu")).also {
            it.requestTTSResourceGet = TTSResourceGet(word = testWord1, lang = LangId(testLang1))
        }
        val answer1 = connection.request(
            /* subject = */ "XXX",
            /* body = */ context1.toByteArray(),
            /* timeout = */ Duration.of(42, ChronoUnit.SECONDS)
        )
        val res1 = ttsContextFromByteArray(answer1.data)
        Assertions.assertEquals(
            testResponseEntity1,
            res1.responseTTSResourceEntity
        ) { "wrong data: ${answer1.data.toString(Charsets.UTF_8)}" }

        val context2 = TTSContext(operation = TTSOperation.GET_RESOURCE, requestAppAuthId = AppAuthId("uuu")).also {
            it.requestTTSResourceGet = TTSResourceGet(word = testWord2, lang = LangId(testLang2))
        }
        val answer2 = connection.request(
            /* subject = */ "XXX",
            /* body = */ context2.toByteArray(),
            /* timeout = */ Duration.of(42, ChronoUnit.SECONDS)
        )
        val res2 = ttsContextFromByteArray(answer2.data)
        Assertions.assertEquals(
            testResponseEntity2,
            res2.responseTTSResourceEntity
        ) { "wrong data: ${answer2.data.toString(Charsets.UTF_8)}" }

        coVerify(exactly = 2) {
            service.containsResource(any())
        }
        coVerify(exactly = 2) {
            service.getResource(any())
        }
        processor.close()
    }

    @Test
    fun `test send message error`() = runBlocking {
        val testWord = "rrr"
        val testLang = "ttt"
        val testRequestId = "$testLang:$testWord"
        val testRequest = TTSResourceGet(word = testWord, lang = LangId(testLang))
        val service = mockk<TextToSpeechService>()
        coEvery {
            service.containsResource(testRequestId)
        } returns true
        coEvery {
            service.getResource(testRequestId)
        } throws IllegalStateException("expected error")
        val processor = NatsTTSServerProcessorImpl(
            service = service,
            topic = "XXX",
            group = "QQQ",
            connectionUrl = connectionUrl,
        )

        TTSServerController(processor).start()
        while (!processor.ready()) {
            Thread.sleep(100)
        }
        val context = TTSContext(operation = TTSOperation.GET_RESOURCE, requestAppAuthId = AppAuthId("uuu")).also {
            it.requestTTSResourceGet = testRequest
        }
        val answer =
            connection.request("XXX", context.toByteArray(), Duration.of(42, ChronoUnit.SECONDS))
        val res = ttsContextFromByteArray(answer.data)
        res.errors.forEach {
            println(it)
        }

        Assertions.assertEquals(1, res.errors.size)
        Assertions.assertEquals("GET_RESOURCE", res.errors[0].code)
        Assertions.assertEquals(testRequest.toString(), res.errors[0].field)
        Assertions.assertEquals("Error while GET_RESOURCE: unexpected exception", res.errors[0].message)
        Assertions.assertInstanceOf(IllegalStateException::class.java, res.errors[0].exception)

        coVerify(exactly = 1) {
            service.containsResource(testRequestId)
        }
        coVerify(exactly = 1) {
            service.getResource(testRequestId)
        }
        processor.close()
    }

}
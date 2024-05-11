package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.TTSContext
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.utils.toByteArray
import com.gitlab.sszuev.flashcards.utils.ttsContextFromByteArray
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
import java.util.concurrent.TimeUnit

@Timeout(value = 420, unit = TimeUnit.SECONDS)
@Testcontainers
internal class RemoteTTSServiceTest {

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
    fun `test receive message success`() = runBlocking {
        val topic = "XXX"
        val group = "XXX"
        val testData1 = ByteArray(42) { 42 }
        val testData2 = ByteArray(42) { 21 }
        val testRequest1 = TTSResourceGet(word = "qqq", lang = LangId("QQ"))
        val testRequest2 = TTSResourceGet(word = "www", lang = LangId("WW"))
        val testDataEntity1 = ResourceEntity(
            TTSResourceId("qqq:qq"),
            data = testData1
        )
        val testDataEntity2 = ResourceEntity(
            TTSResourceId("fff:ff"),
            data = testData2
        )

        // server:
        connection.createDispatcher {
            val context = ttsContextFromByteArray(it.data)
            when (context.requestTTSResourceGet) {
                testRequest1 -> {
                    context.responseTTSResourceEntity = testDataEntity1
                }

                testRequest2 -> {
                    context.responseTTSResourceEntity = testDataEntity2
                }

                else -> Assertions.fail()
            }
            val body = context.toByteArray()
            connection.publish(it.replyTo, body)
        }.subscribe(topic, group).unsubscribe(topic, 2)

        val repo = RemoteTTSService(
            ttsTopic = topic,
            requestTimeoutInMillis = 2000L,
        ) { Nats.connect(connectionUrl) }

        val res1 = repo.getResource(TTSContext().also { it.requestTTSResourceGet = testRequest1 })
        Assertions.assertEquals(res1.responseTTSResourceEntity, testDataEntity1)

        val res2 = repo.getResource(TTSContext().also { it.requestTTSResourceGet = testRequest2 })
        Assertions.assertEquals(res2.responseTTSResourceEntity, testDataEntity2)
    }
}
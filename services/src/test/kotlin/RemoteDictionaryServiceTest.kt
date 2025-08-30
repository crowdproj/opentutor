package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.DictionaryContext
import com.gitlab.sszuev.flashcards.model.common.AppAuthId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.utils.dictionaryContextFromByteArray
import com.gitlab.sszuev.flashcards.utils.toByteArray
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers
internal class RemoteDictionaryServiceTest {
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
    fun `test get dictionaries success`() = runBlocking {
        val topic = "XXX"
        val group = "XXX"
        val testDictionary1 = DictionaryEntity.EMPTY.copy(
            name = "kkk",
            dictionaryId = DictionaryId("KKK")
        )
        val testDictionary2 = DictionaryEntity.EMPTY.copy(
            name = "jjj",
            dictionaryId = DictionaryId("JJJ")
        )
        val testUser1 = AppAuthId("h")
        val testUser2 = AppAuthId("f")

        // server:
        connection.createDispatcher {
            val context = dictionaryContextFromByteArray(it.data)
            when (context.requestAppAuthId) {
                testUser1 -> {
                    context.responseDictionaryEntityList = listOf(testDictionary1)
                }

                testUser2 -> {
                    context.responseDictionaryEntityList = listOf(testDictionary2)
                }

                else -> Assertions.fail()
            }
            val body = context.toByteArray()
            connection.publish(it.replyTo, body)
        }.subscribe(topic, group).unsubscribe(topic, 2)

        val service = RemoteDictionaryService(
            topic = topic,
            requestTimeoutInMillis = 2000L,
            connection = Nats.connect(connectionUrl)
        )

        val res1 = service.getAllDictionaries(DictionaryContext().also { it.requestAppAuthId = testUser1 })
        Assertions.assertEquals(listOf(testDictionary1), res1.responseDictionaryEntityList)

        val res2 = service.getAllDictionaries(DictionaryContext().also { it.requestAppAuthId = testUser2 })
        Assertions.assertEquals(listOf(testDictionary2), res2.responseDictionaryEntityList)
    }
}
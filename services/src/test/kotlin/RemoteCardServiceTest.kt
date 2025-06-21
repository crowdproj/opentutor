package com.gitlab.sszuev.flashcards.services.remote

import com.gitlab.sszuev.flashcards.CardContext
import com.gitlab.sszuev.flashcards.model.domain.CardEntity
import com.gitlab.sszuev.flashcards.model.domain.CardId
import com.gitlab.sszuev.flashcards.model.domain.CardWordEntity
import com.gitlab.sszuev.flashcards.utils.cardContextFromByteArray
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

@Timeout(value = 60, unit = TimeUnit.SECONDS)
@Testcontainers
internal class RemoteCardServiceTest {
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
    fun `test get card success`() = runBlocking {
        val topic = "XXX"
        val group = "XXX"
        val testEntityId1 = "42"
        val testEntityId2 = "24"
        val testEntity1 = CardEntity.EMPTY.copy(
            cardId = CardId(testEntityId1),
            words = listOf(CardWordEntity(word = "w1"))
        )
        val testEntity2 = CardEntity.EMPTY.copy(
            cardId = CardId(testEntityId2),
            words = listOf(CardWordEntity(word = "w2"))
        )

        // server:
        connection.createDispatcher {
            val context = cardContextFromByteArray(it.data)
            when (context.requestCardEntityId.asString()) {
                testEntityId1 -> {
                    context.responseCardEntity = testEntity1
                }

                testEntityId2 -> {
                    context.responseCardEntity = testEntity2
                }

                else -> Assertions.fail()
            }
            val body = context.toByteArray()
            connection.publish(it.replyTo, body)
        }.subscribe(topic, group).unsubscribe(topic, 2)

        val service = RemoteCardService(
            topic = topic,
            requestTimeoutInMillis = 2000L,
            connection = Nats.connect(connectionUrl)
        )

        val res1 = service.getCard(CardContext().also { it.requestCardEntityId = CardId(testEntityId1) })
        Assertions.assertEquals(testEntity1, res1.responseCardEntity)

        val res2 = service.getCard(CardContext().also { it.requestCardEntityId = CardId(testEntityId2) })
        Assertions.assertEquals(testEntity2, res2.responseCardEntity)
    }
}
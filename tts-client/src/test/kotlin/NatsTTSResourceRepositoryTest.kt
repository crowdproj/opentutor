package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.NatsTTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.ServerResourceException
import io.nats.client.Connection
import io.nats.client.Nats
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
internal class NatsTTSResourceRepositoryTest {

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
    fun `test receive message success`() {
        val topic = "XXX"
        val group = "XXX"
        val data = ByteArray(42) { 42 }

        connection.createDispatcher {
            connection.publish(it.replyTo, it.data + data)
        }.subscribe(topic, group).unsubscribe(topic, 2)

        val repo = NatsTTSResourceRepository(
            topic = topic,
            requestTimeoutInMillis = 2000,
        ) { Nats.connect(connectionUrl) }

        val res1 = repo.getResource("xxx")
        Assertions.assertArrayEquals("xxx".toByteArray(Charsets.UTF_8) + data, res1)

        val res2 = repo.getResource("qqq")
        Assertions.assertArrayEquals("qqq".toByteArray(Charsets.UTF_8) + data, res2)
    }

    @Test
    fun `test receive message error`() {
        val topic = "XXX"
        val group = "XXX"
        val data = ByteArray(42) { 42 }

        connection.createDispatcher {
            connection.publish(it.replyTo, "e:".toByteArray(Charsets.UTF_8) + it.data + data)
        }.subscribe(topic, group).unsubscribe(topic, 2)

        val repo = NatsTTSResourceRepository(
            topic = topic,
            requestTimeoutInMillis = 2000,
        ) { Nats.connect(connectionUrl) }

        Assertions.assertThrows(ServerResourceException::class.java) {
            repo.getResource("xxx")
        }
    }
}
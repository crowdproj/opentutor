package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.model.repositories.TTSResourceRepository
import com.rabbitmq.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import org.testcontainers.containers.RabbitMQContainer
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
internal class TTSResourceRepositoryITest {

    companion object {
        private val logger = LoggerFactory.getLogger(TTSResourceRepositoryITest::class.java)

        private val container by lazy {
            RabbitMQContainer("rabbitmq:latest").apply {
                withExposedPorts(5672, 15672)
                withUser("guest", "guest")
                start()
            }
        }
        private val rabbitmqPort: Int by lazy {
            container.getMappedPort(5672)
        }

        private val testConnectionConfig = ConnectionConfig(
            port = rabbitmqPort,
            user = "guest",
            password = "guest",
        )

        private val testQueueConfig = QueueConfig(
            routingKeyIn = "test-in",
            routingKeyOut = "test-out",
            exchangeName = "test-exchange",
            queueName = "test-queue",
            consumerTag = "test-consumer-tag",
        )

        private const val minDelayMillis = 42L

        private lateinit var connection: Connection
        private lateinit var channel: Channel

        @Timeout(value = 420, unit = TimeUnit.SECONDS)
        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            logger.debug("Before.")
            connection = ConnectionFactory().apply {
                host = testConnectionConfig.host
                port = testConnectionConfig.port
                username = testConnectionConfig.user
                password = testConnectionConfig.password
            }.newConnection()
            channel = connection.createChannel()
            channel.exchangeDeclare(testQueueConfig.exchangeName, testQueueConfig.exchangeType)
            channel.queueDeclare(testQueueConfig.queueName, false, false, false, null)
            channel.queueBind(testQueueConfig.queueName, testQueueConfig.exchangeName, testQueueConfig.routingKeyIn)

            val deliverCallback = DeliverCallback { consumerTag, delivery ->
                val requestId = delivery.properties.messageId
                logger.debug("Received by $consumerTag: '$requestId'")
                val size = requestId.replace("^.*\\D(\\d+)$".toRegex(), "$1").toByte()
                val responseData = ByteArray(size.toInt()) { size }
                val responseId = "test-request=$requestId"
                val props = AMQP.BasicProperties.Builder().messageId(responseId).build()
                runBlocking {
                    delay(minDelayMillis)
                }
                channel.basicPublish(
                    testQueueConfig.exchangeName, testQueueConfig.routingKeyOut, props, responseData
                )
            }
            channel.basicConsume(testQueueConfig.queueName, true, deliverCallback, CancelCallback { })
        }

        @Timeout(value = 420, unit = TimeUnit.SECONDS)
        @AfterAll
        @JvmStatic
        fun afterClass() {
            logger.debug("After.")
            channel.close()
            connection.close()
            container.stop()
        }
    }

    @Timeout(value = 420, unit = TimeUnit.SECONDS)
    @Test
    fun `test get single resource success`() = runTest {
        val testRequestId = ResourceId("TestRequestId=42")
        val testAnswer = ByteArray(42) { 42 }
        val repository: TTSResourceRepository = TTSResourceRepositoryImpl(
            connectionConfig = testConnectionConfig,
            queueConfig = testQueueConfig,
            requestTimeoutInMs = 42_000
        )

        val res = repository.getResource(testRequestId)

        Assertions.assertArrayEquals(testAnswer, res.data)
        Assertions.assertTrue(res.errors.isEmpty())
        Assertions.assertEquals(testRequestId, res.cardId)
    }

    @Timeout(value = 420, unit = TimeUnit.SECONDS)
    @Test
    fun `test get resource timeout-cancel error`() = runTest {
        val testRequestId = ResourceId("TestRequestId=42")
        val repository: TTSResourceRepository = TTSResourceRepositoryImpl(
            connectionConfig = testConnectionConfig,
            queueConfig = testQueueConfig,
            requestTimeoutInMs = minDelayMillis - 1
        )

        val res = repository.getResource(testRequestId)

        Assertions.assertEquals(testRequestId, res.cardId)
        Assertions.assertEquals(1, res.errors.size)
        Assertions.assertArrayEquals(ByteArray(0), res.data)
        val error = res.errors[0]
        Assertions.assertEquals("resource", error.code)
        Assertions.assertEquals("exceptions", error.group)
        Assertions.assertInstanceOf(TimeoutCancellationException::class.java, error.exception)
    }
}
package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.speaker.controllers.TextToSpeechController
import com.gitlab.sszuev.flashcards.speaker.services.TextToSpeechService
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.slf4j.LoggerFactory
import org.testcontainers.containers.RabbitMQContainer
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

internal class TextToSpeechControllerITest {

    companion object {
        private val logger = LoggerFactory.getLogger(TextToSpeechControllerITest::class.java)

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

        @AfterAll
        @JvmStatic
        fun afterClass() {
            container.stop()
        }
    }

    @Timeout(value = 420, unit = TimeUnit.SECONDS)
    @Test
    fun `test handle messages`() {
        val testRequestId = "TestRequestId=XXX"
        val testAnswer = ByteArray(42) { 42 }
        val expectedResId = "response={$testRequestId}"
        val mockkTTSService = mockk<TextToSpeechService>()
        every { mockkTTSService.getResource(any()) } returns testAnswer

        TextToSpeechController(
            service = mockkTTSService,
            queueConfig = testQueueConfig,
            connectionConfig = testConnectionConfig
        ).start()
        logger.debug("Controller started.")

        ConnectionFactory().apply {
            host = testConnectionConfig.host
            port = testConnectionConfig.port
            username = testConnectionConfig.user
            password = testConnectionConfig.password
        }.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                var actualResBody: ByteArray? = null
                var actualResId: String? = null
                channel.exchangeDeclare(testQueueConfig.exchangeName, testQueueConfig.exchangeType)
                val queueOut = channel.queueDeclare().queue
                channel.queueBind(queueOut, testQueueConfig.exchangeName, testQueueConfig.routingKeyOut)

                val deliverCallback = DeliverCallback { consumerTag, delivery ->
                    actualResId = delivery.properties.messageId
                    actualResBody = delivery.body
                    logger.debug("Received by $consumerTag: '$actualResId', body=${actualResBody?.contentToString()}")
                }
                channel.basicConsume(queueOut, true, deliverCallback, CancelCallback { })
                val props = AMQP.BasicProperties.Builder().messageId(testRequestId).build()
                channel.basicPublish(testQueueConfig.exchangeName, testQueueConfig.routingKeyIn, props, ByteArray(0))

                runBlocking {
                    withTimeoutOrNull(42.seconds) {
                        while (actualResBody == null) {
                            delay(10)
                        }
                    }
                }

                logger.debug("RESPONSE: ${actualResBody?.contentToString()}")
                Assertions.assertNotNull(actualResBody)
                Assertions.assertNotNull(actualResId)
                Assertions.assertArrayEquals(testAnswer, actualResBody)
                Assertions.assertEquals(expectedResId, actualResId)
            }
        }
    }
}
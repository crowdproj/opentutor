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
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.slf4j.LoggerFactory
import org.testcontainers.containers.RabbitMQContainer
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@Timeout(value = 420, unit = TimeUnit.SECONDS)
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
            routingKeyRequest = "test-in",
            routingKeyResponsePrefix = "test-out=",
            exchangeName = "test-exchange",
            requestQueueName = "test-queue",
            consumerTag = "test-consumer-tag",
        )

        @AfterAll
        @JvmStatic
        fun afterClass() {
            container.stop()
        }

        private fun sendRequest(resourceId: String): Pair<String?, ByteArray?> {
            val responseRoutingKey = testQueueConfig.routingKeyResponsePrefix + resourceId
            ConnectionFactory().apply {
                host = testConnectionConfig.host
                port = testConnectionConfig.port
                username = testConnectionConfig.user
                password = testConnectionConfig.password
            }.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    var responseBody: ByteArray? = null
                    var responseId: String? = null

                    channel.exchangeDeclare(testQueueConfig.exchangeName, "direct")
                    channel.queueDeclare(responseRoutingKey, false, false, false, null)
                    channel.queueBind(responseRoutingKey, testQueueConfig.exchangeName, responseRoutingKey)
                    val deliverCallback = DeliverCallback { consumerTag, delivery ->
                        responseId = delivery.properties.messageId
                        responseBody = delivery.body
                        logger.debug("Received by $consumerTag: '$responseId', body=${responseBody?.contentToString()}")
                    }
                    channel.basicConsume(responseRoutingKey, true, deliverCallback, CancelCallback { })

                    val props = AMQP.BasicProperties.Builder().messageId(resourceId).build()
                    channel.basicPublish(testQueueConfig.exchangeName, testQueueConfig.routingKeyRequest, props, ByteArray(0))

                    runBlocking {
                        withTimeout(42.seconds) {
                            while (responseId == null) {
                                delay(100)
                            }
                        }
                    }

                    logger.debug("RESPONSE BODY: ${responseBody?.contentToString()}")
                    return responseId to responseBody
                }
            }
        }

        private fun prepareRequestQueue() {
            ConnectionFactory().apply {
                host = testConnectionConfig.host
                port = testConnectionConfig.port
                username = testConnectionConfig.user
                password = testConnectionConfig.password
            }.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    channel.exchangeDeclare(testQueueConfig.exchangeName, "direct")
                    channel.queueDeclare(testQueueConfig.requestQueueName, false, false, false, null)
                    channel.queueBind(testQueueConfig.requestQueueName, testQueueConfig.exchangeName, testQueueConfig.routingKeyRequest)
                }
            }
        }
    }

    @Test
    fun `test handle request`() {
        // need to prepare Rabbit MQ request queue since the controller starts asynchronously:
        // the data will be lost if send message too quickly, before consumer creates required queue
        prepareRequestQueue()

        val testRequestId = "TestRequestId=XXX"
        val testAnswer = ByteArray(42) { 42 }
        val expectedResId = "response-success={$testRequestId}"
        val mockkTTSService = mockk<TextToSpeechService>()
        every { mockkTTSService.getResource(any()) } returns testAnswer

        TextToSpeechController(
            service = mockkTTSService,
            queueConfig = testQueueConfig,
            connectionConfig = testConnectionConfig
        ).start()
        logger.debug("Controller started, send request.")

        val res = sendRequest(testRequestId)
        val actualResBody: ByteArray? = res.second
        val actualResId: String? = res.first
        logger.debug("RESPONSE BODY: ${actualResBody?.contentToString()}")
        Assertions.assertNotNull(actualResBody)
        Assertions.assertNotNull(actualResId)
        Assertions.assertArrayEquals(testAnswer, actualResBody)
        Assertions.assertEquals(expectedResId, actualResId)
    }
}
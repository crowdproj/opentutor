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

        @AfterAll
        @JvmStatic
        fun afterClass() {
            container.stop()
        }

        private fun sendRequest(resourceId: String, config: ProcessorConfig): Triple<String?, ByteArray?, Boolean?> {
            val responseRoutingKey = config.routingKeyResponsePrefix + resourceId
            ConnectionFactory().apply {
                host = testConnectionConfig.host
                port = testConnectionConfig.port
                username = testConnectionConfig.user
                password = testConnectionConfig.password
            }.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    var responseBody: ByteArray? = null
                    var responseId: String? = null
                    var responseStatus: Boolean? = null

                    channel.exchangeDeclare(config.exchangeName, "direct")
                    channel.queueDeclare(responseRoutingKey, false, false, false, null)
                    channel.queueBind(responseRoutingKey, config.exchangeName, responseRoutingKey)
                    val deliverCallback = DeliverCallback { consumerTag, delivery ->
                        responseId = delivery.properties.messageId
                        responseBody = delivery.body
                        responseStatus = delivery.properties.headers["status"]?.toString().toBoolean()
                        logger.debug("Received by $consumerTag: '$responseId', body=${responseBody?.contentToString()}")
                    }
                    channel.basicConsume(responseRoutingKey, true, deliverCallback, CancelCallback { })

                    val props = AMQP.BasicProperties.Builder().messageId(resourceId).build()
                    channel.basicPublish(config.exchangeName, config.routingKeyRequest, props, ByteArray(0))

                    runBlocking {
                        withTimeout(42.seconds) {
                            while (responseId == null) {
                                delay(100)
                            }
                        }
                    }

                    logger.debug("RESPONSE BODY: ${responseBody?.contentToString()}")
                    return Triple(responseId, responseBody, responseStatus)
                }
            }
        }

        private fun prepareRequestQueue(config: ProcessorConfig) {
            // need to prepare Rabbit MQ request queue since the controller starts asynchronously:
            // the data will be lost if send message too quickly, before consumer creates required queue
            ConnectionFactory().apply {
                host = testConnectionConfig.host
                port = testConnectionConfig.port
                username = testConnectionConfig.user
                password = testConnectionConfig.password
            }.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    channel.exchangeDeclare(config.exchangeName, "direct")
                    channel.queueDeclare(config.requestQueueName, false, false, false, null)
                    channel.queueBind(
                        config.requestQueueName,
                        config.exchangeName,
                        config.routingKeyRequest
                    )
                }
            }
        }
    }

    @Test
    fun `test handle request success`() {
        val config = ProcessorConfig(
            exchangeName = "test-exchange-ok",
            requestQueueName = "test-q1",
            consumerTag = "test-consumer-tag",
        )
        prepareRequestQueue(config)

        val testRequestId = "testSuccessRequestId"
        val testAnswer = ByteArray(42) { 42 }
        val expectedResId = "response-success=$testRequestId"
        val mockkTTSService = mockk<TextToSpeechService>()
        every { mockkTTSService.getResource(any()) } returns testAnswer

        TextToSpeechController(
            service = mockkTTSService,
            processorConfig = config,
            connectionConfig = testConnectionConfig
        ).start()
        logger.debug("Controller started, send request.")

        val res = sendRequest(testRequestId, config)
        val actualResBody: ByteArray? = res.second
        val actualResId: String? = res.first
        val actualResStatus: Boolean? = res.third
        logger.debug("RESPONSE BODY: ${actualResBody?.contentToString()}")

        Assertions.assertNotNull(actualResBody)
        Assertions.assertNotNull(actualResId)
        Assertions.assertNotNull(actualResStatus)
        Assertions.assertArrayEquals(testAnswer, actualResBody)
        Assertions.assertEquals(expectedResId, actualResId)
        Assertions.assertTrue(actualResStatus!!)
    }

    @Test
    fun `test handle request error`() {
        val config = ProcessorConfig(
            exchangeName = "test-exchange-fail",
            requestQueueName = "test-q2",
            consumerTag = "test-consumer-tag",
        )
        prepareRequestQueue(config)

        val testRequestId = "testErrorRequestId"
        val testException = TestException("test-exception")

        val expectedResId = "response-error=$testRequestId"
        val expectedMessage = "${TestException::class.java.name}: ${testException.message}"

        val mockkTTSService = mockk<TextToSpeechService>()
        every { mockkTTSService.getResource(any()) } throws testException

        TextToSpeechController(
            service = mockkTTSService,
            processorConfig = config,
            connectionConfig = testConnectionConfig
        ).start()
        logger.debug("Controller started, send request.")

        val res = sendRequest(testRequestId, config)
        val actualResBody: String? = res.second?.toString(Charsets.UTF_8)
        val actualResId: String? = res.first
        val actualResStatus: Boolean? = res.third
        logger.debug("RESPONSE BODY: $actualResBody")

        Assertions.assertNotNull(actualResBody)
        Assertions.assertNotNull(actualResId)
        Assertions.assertNotNull(actualResStatus)
        Assertions.assertEquals(expectedResId, actualResId)
        Assertions.assertTrue(actualResBody!!.startsWith(expectedMessage))
        Assertions.assertFalse(actualResStatus!!)
    }

    private class TestException(msg: String) : RuntimeException(msg)
}
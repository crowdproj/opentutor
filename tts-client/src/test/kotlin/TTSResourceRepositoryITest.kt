package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.ServerResourceException
import com.rabbitmq.client.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import org.testcontainers.containers.RabbitMQContainer
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Timeout(value = 420, unit = TimeUnit.SECONDS)
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

        private const val testRequestQueueName = "test-queue"
        private val testConfig = ClientConfig(
            routingKeyRequest = "test-in",
            routingKeyResponsePrefix = "test-out",
            exchangeName = "test-exchange",
            consumerTag = "test-consumer-tag",
        )

        private const val minDelayMillis = 42L

        private lateinit var connection: Connection
        private lateinit var channel: Channel

        @Timeout(value = 420, unit = TimeUnit.SECONDS)
        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            logger.debug("Before. Prepare Test Server")
            connection = ConnectionFactory().apply {
                host = testConnectionConfig.host
                port = testConnectionConfig.port
                username = testConnectionConfig.user
                password = testConnectionConfig.password
            }.newConnection()
            channel = connection.createChannel()
            channel.exchangeDeclare(testConfig.exchangeName, "direct")
            channel.queueDeclare(testRequestQueueName, false, false, false, null)
            channel.queueBind(
                testRequestQueueName,
                testConfig.exchangeName,
                testConfig.routingKeyRequest
            )

            val deliverCallback = DeliverCallback { consumerTag, delivery ->
                val requestId = delivery.properties.messageId
                val responseRoutingKey = testConfig.routingKeyResponsePrefix + requestId
                logger.debug("[$consumerTag] -- received request '$requestId'.")
                val responseId = "test-request=$requestId"
                val props = AMQP.BasicProperties.Builder().messageId(responseId)
                val responseData: ByteArray
                if (mustBeError(requestId)) {
                    responseData = TestException().stackTraceToString().toByteArray(Charsets.UTF_8)
                    props.headers(mapOf("status" to false))
                } else {
                    val size = extractSize(requestId)
                    responseData = ByteArray(size.toInt()) { size }
                    logger.debug("[$consumerTag] -- send the test data size=$size to $responseRoutingKey.")
                    props.headers(mapOf("status" to true))
                }
                runBlocking {
                    delay(minDelayMillis)
                }
                channel.basicPublish(
                    testConfig.exchangeName, responseRoutingKey, props.build(), responseData
                )
            }
            channel.basicConsume(testRequestQueueName, true, deliverCallback, CancelCallback { })
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

        private fun extractSize(requestId: String): Byte {
            return requestId.replace("^.*\\D(\\d+)$".toRegex(), "$1").toByte()
        }

        private fun testRequestId(id: Byte, status: Boolean = true): ResourceId {
            return ResourceId("${if (status) "ok" else "error"}-test-request-id=$id")
        }

        private fun mustBeError(requestId: String): Boolean {
            return requestId.startsWith("error")
        }

        private fun testMultithreadingRun(@Suppress("SameParameterValue") numThreads: Int, runnable: (Int) -> Unit) {
            val service = Executors.newFixedThreadPool(numThreads)
            val futures = (1..numThreads).map {
                service.submit {
                    runnable(it)
                }
            }
            service.shutdown()
            val error = AssertionError()
            futures.forEach {
                try {
                    it.get()
                } catch (ex: Throwable) {
                    error.addSuppressed(ex)
                }
            }
            if (error.suppressed.isNotEmpty()) {
                throw error
            }
        }

        private suspend fun testSendRequestSuccess(repository: TTSResourceRepository, testRequestId: ResourceId) {
            val expectedDataSize = extractSize(testRequestId.asString())
            val expectedDataArray = ByteArray(expectedDataSize.toInt()) { expectedDataSize }
            val res = repository.getResource(testRequestId)
            Assertions.assertArrayEquals(expectedDataArray, res.data) { "expected: $expectedDataSize." }
            Assertions.assertTrue(res.errors.isEmpty())
            Assertions.assertEquals(testRequestId, res.resourceId)
        }
    }

    @Test
    fun `test success get two resource sequentially`() = runTest {
        val repository1: TTSResourceRepository = TTSResourceRepositoryImpl(
            connectionConfig = testConnectionConfig,
            clientConfig = testConfig,
            requestTimeoutInMs = 42_000
        )
        testSendRequestSuccess(repository1, testRequestId(1))

        val repository2: TTSResourceRepository = TTSResourceRepositoryImpl(
            connectionConfig = testConnectionConfig,
            clientConfig = testConfig,
            requestTimeoutInMs = 42_000
        )
        testSendRequestSuccess(repository2, testRequestId(2))
    }

    @Test
    fun `test fail get resources with timeout cancel`() = runTest {
        for (id in 1..3) {
            val testRequestId = testRequestId(id.toByte())
            val repository: TTSResourceRepository = TTSResourceRepositoryImpl(
                connectionConfig = testConnectionConfig,
                clientConfig = testConfig,
                requestTimeoutInMs = minDelayMillis - 1
            )

            val res = repository.getResource(testRequestId)

            Assertions.assertEquals(testRequestId, res.resourceId)
            Assertions.assertEquals(1, res.errors.size)
            Assertions.assertArrayEquals(ByteArray(0), res.data)
            val error = res.errors[0]
            Assertions.assertEquals("resource", error.code)
            Assertions.assertEquals("exceptions", error.group)
            Assertions.assertInstanceOf(TimeoutCancellationException::class.java, error.exception)
        }
    }

    @Test
    fun `test success get resources concurrently with dedicated connections`() {
        val numThreads = 10
        val ids = (1..numThreads / 2).flatMap { sequenceOf(it, it) }.toList()
        val cyclicBarrier = CyclicBarrier(numThreads)
        testMultithreadingRun(numThreads) {
            val repository: TTSResourceRepository = TTSResourceRepositoryImpl(
                connectionConfig = testConnectionConfig,
                clientConfig = testConfig,
                requestTimeoutInMs = 42_000
            )
            val testRequestId = testRequestId(ids[it - 1].toByte())
            cyclicBarrier.await()
            logger.debug("Run in thread for request id=$testRequestId.")
            runBlocking {
                testSendRequestSuccess(repository, testRequestId)
            }
        }
    }

    @Test
    fun `test success get resources concurrently with single connection`() {
        val numThreads = 10
        val ids = (1..numThreads / 2).flatMap { sequenceOf(it, it) }.toList()

        val repository: TTSResourceRepository = TTSResourceRepositoryImpl(
            connectionConfig = testConnectionConfig,
            clientConfig = testConfig,
            requestTimeoutInMs = 42_000
        )
        val cyclicBarrier = CyclicBarrier(numThreads)
        testMultithreadingRun(numThreads) {
            val testRequestId = testRequestId(ids[it - 1].toByte())
            cyclicBarrier.await()
            logger.debug("Run in thread for request id=$testRequestId.")
            runBlocking {
                testSendRequestSuccess(repository, testRequestId)
            }
        }
    }

    @Test
    fun `test fail get resources with server error`() = runTest {
        for (id in 1..3) {
            val testRequestId = testRequestId(id.toByte(), false)
            val repository: TTSResourceRepository = TTSResourceRepositoryImpl(
                connectionConfig = testConnectionConfig,
                clientConfig = testConfig,
                requestTimeoutInMs = 4200,
            )
            val res = repository.getResource(testRequestId)
            Assertions.assertEquals(testRequestId, res.resourceId)
            Assertions.assertEquals(1, res.errors.size)
            Assertions.assertArrayEquals(ByteArray(0), res.data)
            val error = res.errors[0]
            Assertions.assertEquals("resource", error.code)
            Assertions.assertEquals("exceptions", error.group)
            Assertions.assertInstanceOf(ServerResourceException::class.java, error.exception)
            Assertions.assertTrue(error.message.startsWith("[${testRequestId.asString()}]::${TestException::class.java.name}"))
        }
    }

    class TestException : RuntimeException()
}
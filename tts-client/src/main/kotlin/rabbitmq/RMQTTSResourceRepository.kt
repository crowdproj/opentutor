package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceGet
import com.gitlab.sszuev.flashcards.model.domain.TTSResourceId
import com.gitlab.sszuev.flashcards.repositories.TTSResourceEntityResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceIdResponse
import com.gitlab.sszuev.flashcards.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.NotFoundResourceException
import com.gitlab.sszuev.flashcards.speaker.ServerResourceException
import com.gitlab.sszuev.flashcards.speaker.TTSClientConfig
import com.gitlab.sszuev.flashcards.speaker.TTSClientSettings
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Delivery
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

private val logger = LoggerFactory.getLogger(RMQTTSResourceRepository::class.java)

class RMQTTSResourceRepository(
    private val connectionFactory: RabbitmqConnectionFactory,
    private val config: TTSClientConfig,
    private val requestTimeoutInMillis: Long = TTSClientSettings.requestTimeoutInMilliseconds
) : TTSResourceRepository {

    constructor(
        connectionConfig: ConnectionConfig = ConnectionConfig(),
        clientConfig: TTSClientConfig = TTSClientConfig(),
        requestTimeoutInMs: Long = TTSClientSettings.requestTimeoutInMilliseconds
    ) : this(
        connectionFactory = SimpleRabbitmqConnectionFactory(config = connectionConfig),
        config = clientConfig,
        requestTimeoutInMillis = requestTimeoutInMs
    )

    init {
        require(requestTimeoutInMillis != 0L)
        logger.info("TTS-client request timeout=${requestTimeoutInMillis}ms.")
    }

    private val scope = CoroutineScope(
        context = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("thread-rabbitmq-test-client")
    )

    override suspend fun findResourceId(filter: TTSResourceGet): TTSResourceIdResponse {
        return TTSResourceIdResponse(TTSResourceId("${filter.lang.asString()}:${filter.word}"))
    }

    /**
     * Gets the resource data from Rabbit MQ message with timeout.
     * @param [id][TTSResourceId]
     * @return [TTSResourceEntityResponse]
     */
    override suspend fun getResource(id: TTSResourceId): TTSResourceEntityResponse {
        val errors: MutableList<AppError> = mutableListOf()
        val entity = try {
            val res: Deferred<Any> = scope.async(context = Dispatchers.IO) {
                return@async try {
                    retrieveData(id)
                } catch (exception: Exception) {
                    logger.error("Error while getting resource ID={}", id, exception)
                    exception
                }
            }
            val data = if (requestTimeoutInMillis < 0) {
                res.await()
            } else {
                runBlocking {
                    withTimeout(requestTimeoutInMillis) { res.await() }
                }
            }
            when (data) {
                is Exception -> {
                    errors.add(data.asError())
                    ResourceEntity.DUMMY
                }

                is ByteArray -> {
                    ResourceEntity(resourceId = id, data = data)
                }

                else -> {
                    throw IllegalStateException("Unexpected data")
                }
            }
        } catch (ex: Exception) {
            logger.error("Error while getting resource ID={}", id, ex)
            errors.add(ex.asError())
            ResourceEntity.DUMMY
        }
        return TTSResourceEntityResponse(entity, errors)
    }

    /**
     * Gets the byte-array from Rabbit MQ message.
     * @param [id][TTSResourceId]
     * @return [ByteArray]
     * @throws IllegalStateException
     * @throws NotFoundResourceException
     * @throws ServerResourceException
     */
    private suspend fun retrieveData(id: TTSResourceId): ByteArray {
        return connectionFactory.connection.createChannel().use { channel ->
            var res: Any? = null
            val responseRoutingKey = config.routingKeyResponsePrefix + id.asString()
            if (logger.isDebugEnabled) {
                logger.debug("Bind queue with routing-key='$responseRoutingKey'.")
            }
            channel
                .exchange(
                    exchangeName = config.exchangeName,
                    exchangeType = "direct",
                )
                .queue(queueName = responseRoutingKey)
                .bind(
                    queueName = responseRoutingKey,
                    exchangeName = config.exchangeName,
                    routingKey = responseRoutingKey
                )
                .consume(
                    queueName = responseRoutingKey,
                    consumerTag = config.consumerTag,
                    deliverCallback = channel.deliverCallback(
                        publishMessage = { t, m ->
                            res = handleResponse(t, m)
                        }
                    ),
                    cancelCallback = cancelCallback(),
                )
            // send request to in-queue
            channel.sendRequest(
                tag = config.consumerTag,
                requestId = id.asString(),
                targetRoutingKey = config.routingKeyRequest
            )
            while (res == null && channel.isOpen) {
                delay(timeMillis = 10)
            }
            if (!channel.isOpen) {
                logger.info("Channel for [${config.consumerTag}] was closed.")
            }
            if (res == null) {
                channel.abort(506, "Unable to get response")
                throw NotFoundResourceException(id, "null result for request.")
            }
            if (res is ByteArray) {
                val array = res as ByteArray
                if (array.isEmpty()) {
                    throw NotFoundResourceException(id, "empty result for request.")
                }
                return@use array
            }
            if (res is String) {
                throw ServerResourceException(id, res as String)
            }
            throw IllegalStateException("Unknown type ${res!!::class.java.simpleName}")
        }
    }

    private fun handleResponse(tag: String, message: Delivery): Any {
        val responseBody = message.body
        if (logger.isDebugEnabled) {
            val responseId = message.properties.messageId
            logger.debug("[$tag]:: got response message with id={$responseId}.")
        }
        if (isSuccess(message)) {
            return responseBody
        }
        return responseBody.toString(Charsets.UTF_8)
    }

    private fun isSuccess(message: Delivery): Boolean {
        val headers = message.properties.headers
        if (headers == null || headers.isEmpty()) {
            return true
        }
        return headers.getOrDefault(config.messageStatusHeader, true) as Boolean
    }

    private fun Channel.sendRequest(
        tag: String,
        requestId: String,
        targetRoutingKey: String,
    ) {
        if (logger.isDebugEnabled) {
            logger.debug("[$tag]:: send request with id={$requestId} to routingKey='$targetRoutingKey'.")
        }
        val props = AMQP.BasicProperties.Builder().messageId(requestId).build()
        basicPublish(config.exchangeName, targetRoutingKey, props, null)
    }

    private fun Throwable.asError() = AppError(
        code = "resource",
        group = "exceptions",
        field = "",
        message = this.message ?: "",
        exception = this,
    )
}
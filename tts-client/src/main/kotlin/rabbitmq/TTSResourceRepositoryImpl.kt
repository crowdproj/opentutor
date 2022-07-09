package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.domain.LangId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.model.domain.ResourceId
import com.gitlab.sszuev.flashcards.model.repositories.TTSResourceRepository
import com.gitlab.sszuev.flashcards.speaker.NoResourceFoundException
import com.gitlab.sszuev.flashcards.speaker.Settings
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Delivery
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

private val logger = LoggerFactory.getLogger(TTSResourceRepositoryImpl::class.java)

class TTSResourceRepositoryImpl(
    private val connectionFactory: RabbitmqConnectionFactory,
    private val config: QueueConfig,
    private val requestTimeoutInMillis: Long = Settings.requestTimeoutInMilliseconds
) : TTSResourceRepository {

    constructor(connectionConfig: ConnectionConfig, queueConfig: QueueConfig, requestTimeoutInMs: Long) : this(
        connectionFactory = SimpleRabbitmqConnectionFactory(config = connectionConfig),
        config = queueConfig,
        requestTimeoutInMillis = requestTimeoutInMs
    )

    private val scope = CoroutineScope(
        context = Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("thread-rabbitmq-test-client")
    )

    override suspend fun findResourceId(word: String, lang: LangId): ResourceId {
        return ResourceId("${lang.asString()}:$word")
    }

    override suspend fun getResource(id: ResourceId): ResourceEntity {
        val errors: MutableList<AppError> = mutableListOf()
        val data = try {
            retrieveDataWithTimeout(id)
        } catch (ex: Throwable) {
            errors.add(ex.asError())
            ByteArray(0)
        }
        return ResourceEntity(id, data, errors)
    }

    /**
     * @throws TimeoutCancellationException
     * @throws IllegalStateException
     */
    private suspend fun retrieveDataWithTimeout(id: ResourceId): ByteArray {
        val res: Deferred<ByteArray> = scope.async(context = Dispatchers.IO) {
            return@async retrieveData(id)
        }
        return runBlocking {
            withTimeout(requestTimeoutInMillis) { res.await() }
        }
    }

    /**
     * Gets the data from Rabbit MQ message.
     * @param [id][ResourceId]
     * @return [ByteArray]
     * @throws NoResourceFoundException
     * @throws CancellationException
     */
    private suspend fun retrieveData(id: ResourceId): ByteArray {
        return connectionFactory.connection.createChannel().use { channel ->
            var res: ByteArray? = null
            channel
                .exchangeDeclare(config)
                .queueDeclare(config)
                .queueBind(config, config.routingKeyOut)
                .basicConsume(config, channel.deliverCallback(
                    publishMessage = { t, m ->
                        res = handleResponse(t, m)
                    }
                ), cancelCallback())

            channel.sendRequest(id.asString(), config.routingKeyIn)
            while (res == null && channel.isOpen) {
                delay(timeMillis = 10)
            }
            if (!channel.isOpen) {
                logger.info("Channel for [${config.consumerTag}] was closed.")
            }
            if (res == null) {
                channel.abort(506, "Unable to get response")
                throw NoResourceFoundException("Null result for request $id.")
            }
            if (res!!.isEmpty()) {
                throw NoResourceFoundException("Empty result for request $id.")
            }
            return@use res!!
        }
    }

    private fun handleResponse(tag: String, message: Delivery): ByteArray {
        val responseBody = message.body
        if (logger.isDebugEnabled) {
            val responseId = message.properties.messageId
            logger.info("[$tag]:: got response message with id={$responseId}, body length=${responseBody.size}.")
        }
        return responseBody
    }

    private fun Channel.sendRequest(
        requestId: String,
        targetRoutingKey: String,
    ) {
        if (logger.isDebugEnabled) {
            logger.info("Send request with id={$requestId} to $targetRoutingKey")
        }
        val props = AMQP.BasicProperties.Builder().messageId(requestId).build()
        basicPublish(config.exchangeName, targetRoutingKey, props, ByteArray(0))
    }

    private fun Throwable.asError() = AppError(
        code = "resource",
        group = "exceptions",
        field = "",
        message = this.message ?: "",
        exception = this,
    )
}
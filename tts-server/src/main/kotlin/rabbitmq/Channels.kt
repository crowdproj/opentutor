package com.gitlab.sszuev.flashcards.speaker.rabbitmq

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback

internal fun Channel.exchangeDeclare(queueConfig: QueueConfig): Channel {
    this.exchangeDeclare(
        /* exchange = */ queueConfig.exchangeName,
        /* type = */ queueConfig.exchangeType
    )
    return this
}

internal fun Channel.queueDeclare(queueConfig: QueueConfig): Channel {
    queueDeclare(
        /* queue = */
        queueConfig.queueName,
        /* durable = */
        false,
        /* exclusive = */
        false,
        /* autoDelete = */
        false,
        /* arguments = */
        null,
    )
    return this
}

internal fun Channel.queueBind(queueConfig: QueueConfig, routingKeyToListen: String): Channel {
    queueBind(queueConfig.queueName, queueConfig.exchangeName, routingKeyToListen)
    return this
}

internal fun Channel.basicConsume(
    queueConfig: QueueConfig,
    deliverCallback: DeliverCallback,
    cancelCallback: CancelCallback
): Channel {
    basicConsume(
        /* queue = */ queueConfig.queueName,
        /* autoAck = */ true,
        /* consumerTag = */ queueConfig.consumerTag,
        /* deliverCallback = */ deliverCallback,
        /* cancelCallback = */ cancelCallback
    )
    return this
}
